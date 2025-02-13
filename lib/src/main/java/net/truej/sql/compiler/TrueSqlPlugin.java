package net.truej.sql.compiler;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import net.truej.sql.TrueSql;
import net.truej.sql.bindings.NullParameter;
import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.InvocationsFinder.*;
import net.truej.sql.fetch.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.truej.sql.compiler.StatementGenerator.*;

public class TrueSqlPlugin implements Plugin {
    public static final String NAME = "TrueSql";

    // https://static1.srcdn.com/wordpress/wp-content/uploads/2021/05/scary-movie-doofy.jpg?q=50&fit=crop&w=1100&h=618&dpr=1.5

    static java.util.List<Object> doofyEncode(Object value) {
        if (value == null) return null;

        var cl = value.getClass();

        if (java.util.List.class.isAssignableFrom(cl)) {
            var result = new ArrayList<>();
            result.add(cl.getName());
            for (var obj : (java.util.List<Object>) value)
                result.add(doofyEncode(obj));

            return result;
        } if (cl.isRecord()) {
            var result = new ArrayList<>();
            result.add(cl.getName());

            for (var rc : cl.getRecordComponents())
                try {
                    result.add(doofyEncode(rc.getAccessor().invoke(value)));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            return result;
        } else if (cl.isEnum())
            return java.util.List.of(cl.getName(), ((Enum<?>) value).name());
        else
            return new ArrayList<>() {{
                add(null);
                add(value);
            }};
    }

    static Object doofyDecode(Object in) {
        if (in == null) return null;
        var value = (java.util.List<Object>) in;

        try {
            var className = value.get(0);
            if (className == null) return value.get(1);

            Class cl = Class.forName((String) className);

            if (java.util.List.class.isAssignableFrom(cl))
                return value.stream().skip(1).map(TrueSqlPlugin::doofyDecode).toList();
            else if (cl.isEnum())
                return Enum.valueOf(cl, (String) value.get(1));
            else // isRecord
                return cl.getConstructors()[0]
                    .newInstance(value.stream().skip(1).map(TrueSqlPlugin::doofyDecode).toArray());

        } catch (
            ClassNotFoundException | InvocationTargetException |
            InstantiationException | IllegalAccessException e
        ) {
            throw new RuntimeException(e);
        }
    }

    public sealed interface MethodInvocationResult { }

    public static class ValidationException extends RuntimeException {
        final JCTree tree;

        public ValidationException(JCTree tree, String message) {
            super(message);
            this.tree = tree;
        }
    }

    public record ValidationError(JCTree tree, String message) implements MethodInvocationResult {}

    public record CompilationWarning(JCTree tree, String message) { }

    public record FetchInvocation(
        String onDatabase, java.util.List<Standard.Binding> bindings,
        String generatedClassName, String fetchMethodName, int lineNumber,
        java.util.List<CompilationWarning> warnings,
        JCTree.JCIdent sourceExpression, QueryMode queryMode,
        @Nullable java.util.List<JdbcMetadataFetcher.SqlParameterMetadata> parametersMetadata,
        String generatedCode
    ) implements MethodInvocationResult { }

    enum ParameterMode {IN, INOUT, OUT, UNKNOWN}

    @Override public String getName() { return NAME; }


    static String arrayClassNameToSourceCodeType(String className) {
        return switch (className.charAt(0)) {
            case '[' -> arrayClassNameToSourceCodeType(className.substring(1)) + "[]";
            case 'B' -> "byte";
            case 'C' -> "char";
            case 'S' -> "short";
            case 'I' -> "int";
            case 'J' -> "long";
            case 'F' -> "float";
            case 'D' -> "double";
            case 'Z' -> "boolean";
            default -> // 'L'
                className.substring(1, className.length() - 1);
        };
    }

    static String classNameToSourceCodeType(String className) {
        return className.startsWith("[") ?
            arrayClassNameToSourceCodeType(className) : className;
    }

    static String arrayTypeToClassName(Type type) {
        if (type instanceof Type.ArrayType at)
            return "[" + arrayTypeToClassName(at.elemtype);
        else
            return switch (type.getTag()) {
                case BYTE -> "B";
                case CHAR -> "C";
                case SHORT -> "S";
                case INT -> "I";
                case LONG -> "J";
                case FLOAT -> "F";
                case DOUBLE -> "D";
                case BOOLEAN -> "Z";
                default -> "L" + type.tsym.flatName() + ";";
            };
    }

    static String typeToClassName(Type type) {
        return type instanceof Type.ArrayType at
            ? arrayTypeToClassName(at)
            : type.tsym.flatName().toString();
    }

    void checkParameters(
        CompilerMessages messages, Symtab symtab, JCTree.JCCompilationUnit cu,
        JCTree.JCMethodInvocation tree, FetchInvocation invocation
    ) {
        if (invocation.parametersMetadata == null) return;

        interface ParameterChecker {
            void check(
                int parameterIndex, Type javaParameterType, ParameterMode javaParameterMode
            );
        }

        var checkParameter = (ParameterChecker) (pIndex, javaParameterType, javaParameterMode) -> {
            var pMetadata = invocation.parametersMetadata.get(pIndex);

            if (pMetadata.mode() != javaParameterMode)
                throw new ValidationException(
                    tree, "for parameter " + (pIndex + 1) + " expected mode " +
                          pMetadata.mode() + " but has " + javaParameterMode
                );

            if (javaParameterType != symtab.botType) {
                var binding = TypeChecker.getBindingForClass(
                    tree, invocation.bindings, true, typeToClassName(javaParameterType)
                );

                TypeChecker.assertTypesCompatible(
                    invocation.onDatabase,
                    pMetadata.sqlType(), pMetadata.sqlTypeName(), pMetadata.javaClassName(),
                    pMetadata.scale(), binding,
                    (typeKind, expected, has) -> new ValidationException(
                        tree, typeKind + " mismatch for parameter " + (pIndex + 1) +
                              ". Expected " + has + " but has " + expected
                    )
                );
            }
//            else if (pMetadata.nullMode() == GLangParser.NullMode.EXACTLY_NOT_NULL)
//                messages.write(
//                    cu, tree, JCDiagnostic.DiagnosticType.WARNING,
//                    "for parameter " + (pIndex + 1) + " expected not-null values " +
//                    "but passed null literal"
//                );
        };

        var pIndex = 0;

        for (var part : invocation.queryMode.parts())
            switch (part) {
                case TextPart __ -> { }
                case InOrInoutParameter p -> {
                    checkParameter.check(
                        pIndex,
                        p.expression().type,
                        switch (p) {
                            case InParameter __ -> ParameterMode.IN;
                            case InoutParameter __ -> ParameterMode.INOUT;
                        }
                    );
                    pIndex++;
                }
                case OutParameter p -> {
                    checkParameter.check(pIndex, p.toType(), ParameterMode.OUT);
                    pIndex++;
                }
                case UnfoldParameter p -> {
                    var argumentCount = unfoldArgumentsCount(p.extractor());
                    if (p.extractor() == null)
                        checkParameter.check(
                            pIndex, p.expression().type.getTypeArguments().getFirst(), ParameterMode.IN
                        );
                    else
                        for (var i = 0; i < argumentCount; i++)
                            checkParameter.check(
                                pIndex + i, ((JCTree.JCNewArray) p.extractor().body)
                                    .elems.get(i).type, ParameterMode.IN
                            );

                    pIndex += argumentCount;
                }
            }
    }

    static Class<?> primitiveTypeToBoxedClass(Type.JCPrimitiveType pt) {
        return switch (pt.getTag()) {
            case BYTE -> Byte.class;
            case CHAR -> Character.class;
            case SHORT -> Short.class;
            case LONG -> Long.class;
            case FLOAT -> Float.class;
            case INT -> Integer.class;
            case DOUBLE -> Double.class;
            default -> Boolean.class;
        };
    }

    static Type boxType(Names names, Symtab symtab, Type t) {
        var boxed = (Function<Class<?>, Type>) cl ->
            symtab.getClass(symtab.java_base, names.fromString(cl.getName())).type;

        if (t instanceof Type.JCPrimitiveType pt)
            return boxed.apply(primitiveTypeToBoxedClass(pt));

        return t;
    }

    void handle(
        Symtab symtab, Names names, TreeMaker maker,
        CompilerMessages messages, JCTree.JCCompilationUnit cu,
        JCTree.JCMethodInvocation tree, FetchInvocation invocation
    ) {

        checkParameters(messages, symtab, cu, tree, invocation);

        var clParameterExtractor = symtab.getClass(
            symtab.java_base, names.fromString(
                Function.class.getName()
            )
        );

        var clGenerated = symtab.getClass(
            symtab.unnamedModule, names.fromString(invocation.generatedClassName)
        );

        var mtGenerated = (Symbol.MethodSymbol) clGenerated.members()
            .getSymbolsByName(names.fromString(
                invocation.fetchMethodName + "__line" + invocation.lineNumber + "__"
            )).iterator().next();

        tree.meth = maker.Select(maker.Ident(clGenerated), mtGenerated);
        tree.args = List.nil();

        var createRwFor = (Function<Type, JCTree.JCExpression>) type -> {
            final Symbol.ClassSymbol rwClassSymbol;

            if (type == symtab.botType)
                rwClassSymbol = symtab.getClass(
                    symtab.unnamedModule, names.fromString(NullParameter.class.getName())
                );
            else {
                var forClassName = typeToClassName(type);

                var binding = TypeChecker.getBindingForClass(
                    tree, invocation.bindings, true, forClassName
                );

                rwClassSymbol = symtab.getClass(
                    symtab.unnamedModule, names.fromString(binding.rwClassName())
                );
            }

            var rwClassConstructor = (Symbol.MethodSymbol) rwClassSymbol.members().getSymbols(sym ->
                sym instanceof Symbol.MethodSymbol m && m.name.equals(names.fromString("<init>")) &&
                m.params.isEmpty()
            ).iterator().next();

            var newClass = maker.NewClass(null, List.nil(), maker.Ident(rwClassSymbol), List.nil(), null);
            newClass.type = new Type.ClassType(Type.noType, List.nil(), rwClassSymbol);
            newClass.constructor = rwClassConstructor;
            newClass.constructor.type = rwClassConstructor.type;

            return newClass;
        };

        switch (invocation.queryMode) {
            case BatchedQuery bq:
                tree.args = tree.args.append(bq.listDataExpression());

                for (var part : bq.parts())
                    switch (part) {
                        case InParameter p:
                            var extractor = new JCTree.JCLambda(
                                List.of(bq.extractor().params.head),
                                p.expression()
                            );
                            var extractorType = new Type.ClassType(
                                Type.noType,
                                List.of(
                                    bq.extractor().params.head.type,
                                    boxType(names, symtab, p.expression().type)
                                ),
                                clParameterExtractor
                            );

                            extractor.type = extractorType;
                            extractor.target = extractorType;

                            tree.args = tree.args.append(extractor);
                            tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                            break;
                        default:
                            break;
                    }
                break;
            case SingleQuery sq:
                for (var part : sq.parts())
                    switch (part) {
                        case InOrInoutParameter p:
                            tree.args = tree.args.append(p.expression());
                            tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                            break;
                        case OutParameter p:
                            tree.args = tree.args.append(
                                createRwFor.apply(p.toType())
                            );
                            break;
                        case UnfoldParameter p:
                            var n = unfoldArgumentsCount(p.extractor());
                            var unfoldType = p.expression().type.allparams().head;
                            tree.args = tree.args.append(p.expression());


                            if (p.extractor() == null) {
                                tree.args = tree.args.append(createRwFor.apply(
                                    unfoldType
                                ));
                            } else
                                for (var i = 0; i < n; i++) {
                                    var partExpression =
                                        ((JCTree.JCNewArray) p.extractor().body).elems.get(i);

                                    var extractor = new JCTree.JCLambda(
                                        List.of(p.extractor().params.head), partExpression
                                    );

                                    var extractorType = new Type.ClassType(
                                        Type.noType,
                                        List.of(
                                            p.extractor().params.head.type,
                                            boxType(names, symtab, partExpression.type)
                                        ),
                                        clParameterExtractor
                                    );

                                    extractor.type = extractorType;
                                    extractor.target = extractorType;

                                    tree.args = tree.args.append(extractor);
                                    tree.args = tree.args.append(createRwFor.apply(partExpression.type));
                                }

                            break;
                        case TextPart __:
                            break;
                    }
                break;
        }

        tree.args = tree.args.append(invocation.sourceExpression);
    }

    HashSet<Symbol.MethodSymbol> trueSqlDslMethods = null;

    void addSymbol(HashSet<Symbol.MethodSymbol> to, Symbol.ClassSymbol clSym) {
        clSym.members().getSymbols().forEach(sym -> {
            if (sym instanceof Symbol.MethodSymbol mt)
                to.add(mt);
            if (sym instanceof Symbol.ClassSymbol cl)
                addSymbol(to, cl);
        });
    }

    HashSet<Symbol.MethodSymbol> parseDslMethods(Symtab symtab, Names names, Class<?>... classes) {
        var to = new HashSet<Symbol.MethodSymbol>();
        for (var aClass : classes)
            addSymbol(
                to, symtab.enterClass(
                    symtab.unnamedModule, names.fromString(aClass.getName())
                )
            );
        return to;
    }

    boolean isTrueSqlDslInvocation(Symtab symtab, Names names, JCTree.JCMethodInvocation tree) {
        if (trueSqlDslMethods == null)
            trueSqlDslMethods = parseDslMethods(
                symtab, names, As.class, Q.class, NewConstraint.class,
                Parameters.class, UpdateCount.class, NoUpdateCount.class
            );

        final Symbol symbol;

        if (tree.meth instanceof JCTree.JCIdent id)
            symbol = id.sym;
        else
            symbol = ((JCTree.JCFieldAccess) tree.meth).sym;

        return symbol instanceof Symbol.MethodSymbol mt && trueSqlDslMethods.contains(mt);
    }

    void assertHasNoDanglingTrueSqlCalls(
        Symtab symtab, Names names, JCTree.JCCompilationUnit cu
    ) {

        var hasTrueSqlAnnotation = new boolean[]{false};

        cu.accept(
            new TreeScanner() {
                @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                    if (tree.type.tsym.getAnnotation(TrueSql.class) != null)
                        hasTrueSqlAnnotation[0] = true;

                    super.visitClassDef(tree);
                }
            }
        );

        cu.accept(
            new TreeScanner() {
                @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                    super.visitApply(tree);

                    if (
                        isTrueSqlDslInvocation(symtab, names, tree) &&
                            !(tree.meth instanceof JCTree.JCFieldAccess fa &&
                            fa.name.contentEquals("constraint"))
                    ) {
                        if (!hasTrueSqlAnnotation[0])
                            throw new ValidationException(
                                tree, "TrueSql DSL used but class not annotated with @TrueSql"
                            );
                        throw new ValidationException(
                            tree, "Incorrect TrueSql DSL usage - dangling call"
                        );
                    }
                }
            }
        );
    }

    @Override public void init(JavacTask task, String... args) {

        task.addTaskListener(new TaskListener() {
            @Override public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {

                    var context = ((BasicJavacTask) task).getContext();
                    var symtab = Symtab.instance(context);
                    var names = Names.instance(context);
                    var maker = TreeMaker.instance(context);
                    var messages = new CompilerMessages(context);

                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    @SuppressWarnings("unchecked") var dataFromAnnotationProcessor = (
                        HashMap<
                            JCTree.JCCompilationUnit,
                            HashMap<JCTree.JCMethodInvocation, Object>
                            >
                        ) context.get(HashMap.class);

                    var hasNoErrors = true;
                    var sendCompilationError = (Consumer<ValidationError>) error ->
                        messages.write(
                            (JCTree.JCCompilationUnit) e.getCompilationUnit(),
                            error.tree, JCDiagnostic.DiagnosticType.ERROR, error.message
                        );

                    if (dataFromAnnotationProcessor != null) {
                        var cuTrees = dataFromAnnotationProcessor.get(cu);

                        if (cuTrees != null)
                            for (var kv : cuTrees.entrySet()) {
                                var tree = kv.getKey();
                                var invocation = (MethodInvocationResult) doofyDecode(kv.getValue());

                                if (!isTrueSqlDslInvocation(symtab, names, tree))
                                    continue;

                                try {
                                    switch (invocation) {
                                        case FetchInvocation fi -> {
                                            for (var warning : fi.warnings)
                                                messages.write(
                                                    cu, warning.tree, JCDiagnostic.DiagnosticType.WARNING, warning.message
                                                );

                                            handle(symtab, names, maker, messages, cu, tree, fi);
                                        }
                                        case ValidationError error -> {
                                            hasNoErrors = false;
                                            sendCompilationError.accept(error);
                                        }
                                    }
                                } catch (ValidationException ex) {
                                    hasNoErrors = false;
                                    sendCompilationError.accept(
                                        new ValidationError(ex.tree, ex.getMessage())
                                    );
                                }
                            }
                    }

                    try {
                        if (hasNoErrors)
                            assertHasNoDanglingTrueSqlCalls(symtab, names, cu);
                    } catch (ValidationException ex) {
                        sendCompilationError.accept(
                            new ValidationError(ex.tree, ex.getMessage())
                        );
                    }
                }
            }
        });
    }
}
