package net.truej.sql.compiler;

import com.sun.source.util.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.TrueSql;
import net.truej.sql.bindings.NullParameter;
import net.truej.sql.bindings.Standard;
import net.truej.sql.dsl.As;
import net.truej.sql.dsl.NoUpdateCount;
import net.truej.sql.dsl.Q;
import net.truej.sql.dsl.UpdateCount;
import net.truej.sql.source.ParameterExtractor;
import net.truej.sql.source.Parameters;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.truej.sql.compiler.StatementGenerator.*;

public class TrueSqlPlugin implements Plugin {
    public static final String NAME = "TrueSql";

    // https://static1.srcdn.com/wordpress/wp-content/uploads/2021/05/scary-movie-doofy.jpg?q=50&fit=crop&w=1100&h=618&dpr=1.5

    public static java.util.List<Object> doofyEncode(Object value) {
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

    private static Object doofyDecode(Object in) {
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

    public record Invocation(
        String onDatabase, java.util.List<Standard.Binding> bindings,
        String generatedClassName, String fetchMethodName, int lineNumber,
        JCTree.JCIdent sourceExpression, QueryMode queryMode,
        @Nullable java.util.List<ParameterMetadata> parametersMetadata
    ) { }

    enum ParameterMode {IN, INOUT, OUT, UNKNOWN}

    public record ParameterMetadata(
        String javaClassName, String sqlTypeName, int sqlType, int scale, ParameterMode mode
    ) { }

    @Override public String getName() { return NAME; }

    // FIXME: check parameter mode ???
    void checkParameters(Symtab symtab, Invocation invocation) {
        if (invocation.parametersMetadata == null) return;

        var checkParameterType = (BiConsumer<Symbol.TypeSymbol, Integer>)
            (parameterTsym, pIndex) -> {
                // FIXME: reverse expected & has
                var binding = TypeChecker.getBindingForClass(
                    invocation.bindings,
                    parameterTsym.flatName().toString(),
                    GLangParser.NullMode.DEFAULT_NOT_NULL
                );
                var pMetadata = invocation.parametersMetadata.get(pIndex);

                TypeChecker.assertTypesCompatible(
                    invocation.onDatabase,
                    pIndex + 1,
                    pMetadata.sqlType,
                    pMetadata.sqlTypeName,
                    pMetadata.javaClassName,
                    pMetadata.scale,
                    "parameter" + pIndex,
                    binding
                );
            };

        var pIndex = 0;

        for (var part : invocation.queryMode.parts())
            switch (part) {
                case InvocationsFinder.TextPart _ -> { }
                case InvocationsFinder.InOrInoutParameter p -> {
                    if (p.expression().type != symtab.botType)
                        checkParameterType.accept(p.expression().type.tsym, pIndex);
                    pIndex++;
                }
                case InvocationsFinder.OutParameter p -> {
                    checkParameterType.accept(p.toClass(), pIndex);
                    pIndex++;
                }
                case InvocationsFinder.UnfoldParameter p -> {
                    var argumentCount = StatementGenerator.unfoldArgumentsCount(p.extractor());
                    if (p.extractor() == null) {
                        if (p.expression().type == symtab.botType)
                            // FIXME: use special exception
                            throw new RuntimeException("null cannot be passed as argument for unfold parameter");

                        checkParameterType.accept(
                            p.expression().type.getTypeArguments().getFirst().tsym, pIndex
                        );
                    } else
                        for (var i = 0; i < argumentCount; i++)
                            checkParameterType.accept(
                                ((JCTree.JCNewArray) p.extractor().body)
                                    .elems.get(i).type.tsym, pIndex + i
                            );

                    pIndex += argumentCount;
                }
            }
    }

    void handle(
        Symtab symtab, Names names, TreeMaker maker,
        JCTree.JCMethodInvocation tree, Invocation invocation
    ) {

        checkParameters(symtab, invocation);

        var clParameterExtractor = symtab.getClass(
            symtab.unnamedModule, names.fromString(
                ParameterExtractor.class.getName()
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
                var forClassName = type.tsym.flatName().toString();

                var binding = invocation.bindings.stream().filter(b ->
                    b.className().equals(forClassName)
                ).findFirst().orElseThrow(() -> new InvocationsFinder.ValidationException(
                    "cannot find binding for " + forClassName
                ));

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

        var boxType = (Function<Type, Type>) t -> {
            if (t == symtab.booleanType)
                return symtab.getClass(symtab.java_base, names.fromString(Boolean.class.getName())).type;
            if (t == symtab.byteType)
                return symtab.getClass(symtab.java_base, names.fromString(Byte.class.getName())).type;
            if (t == symtab.charType)
                return symtab.getClass(symtab.java_base, names.fromString(Character.class.getName())).type;
            if (t == symtab.shortType)
                return symtab.getClass(symtab.java_base, names.fromString(Short.class.getName())).type;
            if (t == symtab.intType)
                return symtab.getClass(symtab.java_base, names.fromString(Integer.class.getName())).type;
            if (t == symtab.longType)
                return symtab.getClass(symtab.java_base, names.fromString(Long.class.getName())).type;
            if (t == symtab.floatType)
                return symtab.getClass(symtab.java_base, names.fromString(Float.class.getName())).type;
            if (t == symtab.doubleType)
                return symtab.getClass(symtab.java_base, names.fromString(Double.class.getName())).type;

            return t;
        };

        var metadataIndex = 0;

        switch (invocation.queryMode) {
            case BatchedQuery bq:
                tree.args = tree.args.append(bq.listDataExpression());

                for (var part : bq.parts())
                    switch (part) {
                        case InvocationsFinder.SimpleParameter p:
                            var extractor = new JCTree.JCLambda(
                                List.of(bq.extractor().params.head),
                                p.expression()
                            );
                            var extractorType = new Type.ClassType(
                                Type.noType,
                                List.of(
                                    bq.extractor().params.head.type,
                                    boxType.apply(p.expression().type)
                                ),
                                clParameterExtractor
                            );

                            extractor.type = extractorType;
                            extractor.target = extractorType;

                            tree.args = tree.args.append(extractor);
                            tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                            metadataIndex++;
                            break;
                        case InvocationsFinder.TextPart _:
                            break;
                        case InvocationsFinder.InoutParameter _,
                             InvocationsFinder.OutParameter _,
                             InvocationsFinder.UnfoldParameter _:
                            throw new RuntimeException("unreachable");
                    }
                break;
            case SingleQuery sq:
                for (var part : sq.parts())
                    switch (part) {
                        case InvocationsFinder.SimpleParameter p:
                            tree.args = tree.args.append(p.expression());
                            tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                            metadataIndex++;
                            break;
                        case InvocationsFinder.InoutParameter p:
                            tree.args = tree.args.append(p.expression());
                            tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                            metadataIndex++;
                            break;
                        case InvocationsFinder.OutParameter p:
                            tree.args = tree.args.append(
                                createRwFor.apply(
                                    new Type.ClassType(Type.noType, List.nil(), p.toClass())
                                )
                            );
                            metadataIndex++;
                            break;
                        case InvocationsFinder.UnfoldParameter p:
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
                                            boxType.apply(partExpression.type)
                                        ),
                                        clParameterExtractor
                                    );

                                    extractor.type = extractorType;
                                    extractor.target = extractorType;

                                    tree.args = tree.args.append(extractor);
                                    tree.args = tree.args.append(createRwFor.apply(partExpression.type));
                                    metadataIndex++;
                                }

                            metadataIndex += n;
                            break;
                        case InvocationsFinder.TextPart _:
                            break;
                    }
                break;
        }

        tree.args = tree.args.append(invocation.sourceExpression);
    }

    static class TrueSqlApiCalls {
        final Symtab symtab;
        final Names names;

        HashSet<Symbol.MethodSymbol> all = new HashSet<>();
        HashSet<Symbol.MethodSymbol> q = new HashSet<>();
        HashSet<Symbol.MethodSymbol> fetch = new HashSet<>();

        @SafeVarargs final void addClass(Class<?> aClass, HashSet<Symbol.MethodSymbol>... to) {
            symtab.enterClass(
                symtab.unnamedModule, names.fromString(aClass.getName())
            ).members().getSymbols().forEach(sym -> {
                if (sym instanceof Symbol.MethodSymbol mt)
                    for (var set : to) set.add(mt);
            });
        }

        public TrueSqlApiCalls(Symtab symtab, Names names) {
            this.symtab = symtab;
            this.names = names;

            addClass(Q.class, all, q);
            addClass(As.class, all);

            addClass(UpdateCount.None.class, all, fetch);
            addClass(UpdateCount.One.class, all, fetch);
            addClass(UpdateCount.OneG.class, all, fetch);
            addClass(UpdateCount.OneOrZero.class, all, fetch);
            addClass(UpdateCount.OneOrZeroG.class, all, fetch);
            addClass(UpdateCount.List_.class, all, fetch);
            addClass(UpdateCount.ListG.class, all, fetch);
            addClass(UpdateCount.Stream_.class, all, fetch);
            addClass(UpdateCount.StreamG.class, all, fetch);

            addClass(NoUpdateCount.None.class, all, fetch);
            addClass(NoUpdateCount.One.class, all, fetch);
            addClass(NoUpdateCount.OneG.class, all, fetch);
            addClass(NoUpdateCount.OneOrZero.class, all, fetch);
            addClass(NoUpdateCount.OneOrZeroG.class, all, fetch);
            addClass(NoUpdateCount.List_.class, all, fetch);
            addClass(NoUpdateCount.ListG.class, all, fetch);
            addClass(NoUpdateCount.Stream_.class, all, fetch);
            addClass(NoUpdateCount.StreamG.class, all, fetch);

            addClass(Parameters.class, all);
        }
    }

    TrueSqlApiCalls apiCalls = null;

    static class BadApiUsageException extends RuntimeException {
        final JCTree tree;

        BadApiUsageException(JCTree tree, String message) {
            super(message);
            this.tree = tree;
        }
    }

    void assertHasNowDanglingTrueSqlCalls(
        Symtab symtab, Names names, Trees trees, JCTree.JCCompilationUnit cu
    ) {
        if (apiCalls == null)
            apiCalls = new TrueSqlApiCalls(symtab, names);

        var hasTrueSqlAnnotation = new boolean[]{false};

        cu.accept(new TreeScanner() {
            @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                if (tree.type.tsym.getAnnotation(TrueSql.class) != null) {
                    hasTrueSqlAnnotation[0] = true;
                    if (tree.type.tsym.getAnnotation(Processed.class) == null)
                        throw new BadApiUsageException(
                            tree, "TrueSql annotation processor not enabled. Check out your " +
                                  "build tool configuration (Gradle, Maven, ...)"
                        );
                }

                super.visitClassDef(tree);
            }
        });

        cu.accept(new TreeScanner() {
            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                super.visitApply(tree);

                final Symbol symbol;

                if (tree.meth instanceof JCTree.JCIdent id)
                    symbol = id.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess fa)
                    symbol = fa.sym;
                else
                    return;

                if (apiCalls.all.contains(symbol)) {
                    if (!hasTrueSqlAnnotation[0])
                        throw new BadApiUsageException(
                            tree, "TrueSql DSL used but class not annotated with @TrueSql"
                        );

                    if (
                        tree.meth instanceof JCTree.JCFieldAccess fa &&
                        apiCalls.q.contains(symbol)
                    ) {
                        if (!(fa.selected instanceof JCTree.JCIdent))
                            throw new BadApiUsageException(
                                tree, "Expected identifier for source for `.q`"
                            );

                        var idSym = ((JCTree.JCIdent) fa.selected).sym;
                        var idTree = trees.getTree(idSym);

                        if (!(
                            idSym.owner instanceof Symbol.ClassSymbol ||
                            (
                                idSym.owner instanceof Symbol.MethodSymbol &&
                                (idSym.flags() & Flags.PARAMETER) != 0L
                            ) ||
                            (
                                idSym.owner instanceof Symbol.MethodSymbol &&
                                idTree instanceof JCTree.JCVariableDecl varDecl &&
                                varDecl.init instanceof JCTree.JCNewClass
                            )
                        ))
                            throw new BadApiUsageException(
                                tree, "Source identifier may be method parameter, class field or " +
                                      "local variable initialized by new (var ds = new MySourceW(...)). " +
                                      "Type of source identifier cannot be generic parameter"
                            );

                        if (!(
                            (
                                tree.args.get(0) instanceof JCTree.JCLiteral lit1 &&
                                lit1.getValue() instanceof String
                            ) || (
                                tree.args.length() == 3 &&
                                tree.args.get(1) instanceof JCTree.JCLiteral lit2 &&
                                lit2.getValue() instanceof String
                            )
                        ))
                            throw new BadApiUsageException(tree, "Query text must be a string literal");

                        if (tree.args.length() == 3) // batch
                            if (!(
                                tree.args.get(2) instanceof JCTree.JCLambda lmb &&
                                lmb.body instanceof JCTree.JCExpression body &&
                                body instanceof JCTree.JCNewArray newArray &&
                                newArray.elemtype instanceof JCTree.JCIdent elemTypeId &&
                                elemTypeId.sym == symtab.objectType.tsym
                            ))
                                throw new BadApiUsageException(
                                    tree, "Batch parameter extractor must be lambda literal returning " +
                                          "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
                                );
                    }

                    if (apiCalls.fetch.contains(symbol) && !tree.args.isEmpty())
                        if (!(
                            tree.args.getLast() instanceof JCTree.JCFieldAccess fa
                        ))
                            throw new BadApiUsageException(
                                tree, "Incorrect TrueSql DSL usage - dangling call"
                            );

                    throw new BadApiUsageException(
                        tree, "Incorrect TrueSql DSL usage - dangling call"
                    );
                }
            }
        });
    }

    @Override public void init(JavacTask task, String... args) {

        task.addTaskListener(new TaskListener() {
//            @Override public void started(TaskEvent e) {
//                if (e.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
//                    System.out.println("Annotation processor round");
//                }
//            }

            @Override public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {

                    var context = ((BasicJavacTask) task).getContext();
                    var symtab = Symtab.instance(context);
                    var names = Names.instance(context);
                    var maker = TreeMaker.instance(context);
                    var env = JavacProcessingEnvironment.instance(context);
                    var trees = Trees.instance(env);
                    var messages = new CompilerMessages(context);

                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    var dataFromAnnotationProcessor =
                        (HashMap<JCTree.JCCompilationUnit, HashMap<JCTree.JCMethodInvocation, Object>>)
                            context.get(HashMap.class);

                    if (dataFromAnnotationProcessor != null) {
                        var cuTrees = dataFromAnnotationProcessor.get(cu);

                        if (cuTrees != null) {
                            // 4. check parameter types - parse parameter metadata ???
                            cuTrees.forEach((tree, invEncoded) -> {
                                var invocation = (Invocation) doofyDecode(invEncoded);
                                try {
                                    handle(symtab, names, maker, tree, invocation);
                                } catch (InvocationsFinder.ValidationException ex) {
                                    messages.write(
                                        (JCTree.JCCompilationUnit) e.getCompilationUnit(),
                                        tree, JCDiagnostic.DiagnosticType.ERROR, ex.getMessage()
                                    );
                                }
                            });
                        }

                        var xxx = 1;
                    }

                    try {
                        assertHasNowDanglingTrueSqlCalls(symtab, names, trees, cu);
                    } catch (BadApiUsageException ex) {
                        messages.write(
                            cu, ex.tree, JCDiagnostic.DiagnosticType.ERROR, ex.getMessage()
                        );
                    }
                }
            }
        });
    }
}
