package com.truej.sql.v3.compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;

import com.sun.source.util.Trees;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Name;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.model.JavacElements;
import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.config.Configuration;
import com.truej.sql.v3.config.TypeBinding;
import org.jetbrains.annotations.Nullable;

import static com.sun.tools.javac.tree.JCTree.*;

@SupportedAnnotationTypes({"com.truej.sql.v3.TrueSql", "com.truej.sql.v3.config.Configuration"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TrueSqlAnnotationProcessor extends AbstractProcessor {

    // TODO: support for batches

    record ForPrepareInvocation(JCTree tree, List<QueryPart> queryParts) { }
    record SafeFetchInvocation(
        JCTree forPrepare, JCTree mapper, @Nullable String[] generatedKeys
    ) { }

    sealed interface GeneratedKeys { }
    record GeneratedKeysColumnNames(String[] columnNames) implements GeneratedKeys { }
    record GeneratedKeysColumnNumbers(int[] columnNumbers) implements GeneratedKeys { }

    sealed interface QueryPart { }
    sealed interface BatchedQueryPart extends QueryPart { }
    sealed interface SingleQueryPart extends QueryPart { }

    public record TextPart(String text) implements SingleQueryPart, BatchedQueryPart { }
    public record SimpleParameter(JCExpression expression) implements SingleQueryPart, BatchedQueryPart { }
    public record InoutParameter(JCExpression expression) implements SingleQueryPart { }
    public record OutParameter() implements SingleQueryPart { }
    public record UnfoldParameter(int n, JCExpression expression) implements SingleQueryPart { }

    static List<QueryPart> parseQuery(
        Symtab symtab, JCCompilationUnit cu,
        Names names, JCStringTemplate tree
    ) {
        var clParameters = symtab.enterClass(
            cu.modle, names.fromString("com.truej.sql.v3.prepare.Parameters")
        );

        interface ParseLeaf {
            QueryPart parse(Name name, JCMethodInvocation invoke, boolean checkImport);
        }

        var map = (ParseLeaf) (name, invoke, checkImport) -> {
            var check = (Function<String, Boolean>) (sName) -> {
                if (name.equals(names.fromString(sName))) {
                    if (!checkImport) return true;
                    var imp = cu.starImportScope.findFirst(name);
                    return imp != null &&
                           imp instanceof Symbol.MethodSymbol &&
                           imp.owner == clParameters;
                }
                return false;
            };

            if (check.apply("inout")) {
                return new InoutParameter(invoke.args.head);
            } else if (check.apply("out")) {
                return new OutParameter();
            } else if (check.apply("unfold")) {
                return new UnfoldParameter(1, invoke.args.head);
            } else if (check.apply("unfold2")) {
                return new UnfoldParameter(2, invoke.args.head);
            } else if (check.apply("unfold3")) {
                return new UnfoldParameter(3, invoke.args.head);
            } else if (check.apply("unfold4")) {
                return new UnfoldParameter(4, invoke.args.head);
            } else
                return new SimpleParameter(invoke);
        };

        var result = new ArrayList<QueryPart>();
        for (var i = 0; i < tree.fragments.size(); i++) {
            result.add(new TextPart(tree.fragments.get(i)));
            if (i < tree.expressions.size()) {
                var expression = tree.expressions.get(i);
                final QueryPart parsed;

                if (expression instanceof JCMethodInvocation invoke) {
                    if (invoke.meth instanceof JCIdent id) {
                        parsed = map.parse(id.name, invoke, true);
                    } else if (
                        invoke.meth instanceof JCFieldAccess fa &&
                        fa.selected instanceof JCIdent id &&
                        id.name.equals(names.fromString("Parameters"))
                    ) {
                        var imp = cu.namedImportScope.findFirst(id.name);
                        if (imp != null && imp.type.tsym == clParameters)
                            parsed = map.parse(fa.name, invoke, false);
                        else
                            parsed = new SimpleParameter(invoke);
                    } else
                        parsed = new SimpleParameter(invoke);
                } else
                    parsed = new SimpleParameter(expression);

                result.add(parsed);
            }
        }

        return result;
    }

    static Symbol.ClassSymbol resolveType(
        Names names, Symtab symtab, JCCompilationUnit cu, JCExpression tree
    ) {
        var badFormat = (Supplier<RuntimeException>) () ->
            new RuntimeException("expected %SimpleName%.class or %full.qualified.Name%.class");

        if (tree instanceof JCFieldAccess fa) {
            if (!fa.name.equals(names.fromString("class")))
                throw badFormat.get();

            var found = findType(symtab, cu, fa.selected);
            if (found == null)
                throw badFormat.get();

            return found;
        } else
            throw badFormat.get();
    }

    static Symbol.ClassSymbol findType(
        Symtab symtab, JCCompilationUnit cu, JCExpression tree
    ) {
        Predicate<Symbol> isClass = s -> s instanceof Symbol.ClassSymbol;

        if (tree instanceof JCIdent id) {
            var found = (Symbol.ClassSymbol) cu.toplevelScope.findFirst(id.name, isClass);

            if (found == null || found.type.isErroneous())
                found = (Symbol.ClassSymbol) cu.namedImportScope.findFirst(id.name, isClass);

            if (found == null || found.type.isErroneous())
                found = symtab.getClass(cu.modle, cu.packge.fullname.append('.', id.name));

            if (found == null || found.type.isErroneous())
                found = (Symbol.ClassSymbol) cu.starImportScope.findFirst(id.name, isClass);

            if (found == null || found.type.isErroneous())
                return null;

            return found;

        } else if (tree instanceof JCFieldAccess tail) {
            var fqn = tail.name;
            while (true) {
                if (tail.selected instanceof JCIdent id) {
                    fqn = id.name.append('.', fqn); break;
                } else if (tail.selected instanceof JCFieldAccess tfa) {
                    fqn = tfa.name.append('.', fqn); tail = tfa;
                } else
                    return null;
            }

            var found = symtab.getClass(cu.modle, fqn);

            if (found == null || found.type.isErroneous())
                found = symtab.getClass(symtab.java_base, fqn);

            if (found == null || found.type.isErroneous())
                return null;

            return found;
        } else
            return null;
    }

    List<SafeFetchInvocation> findFetchInvocations(
        Names names, Symtab symtab, JCCompilationUnit cu, JCTree tree
    ) {
        var invocations = new ArrayList<SafeFetchInvocation>();

        new TreeScanner() {
            final Map<Name, Symbol.ClassSymbol> varTypes = new HashMap<>();

            @Override public void visitVarDef(JCVariableDecl tree) {
                if (tree.sym == null) {
                    if (tree.vartype != null)
                        varTypes.put(tree.name, TrueSqlAnnotationProcessor.findType(symtab, cu, tree.vartype));
                    else {
                        if (tree.init instanceof JCNewClass cl)
                            varTypes.put(tree.name, TrueSqlAnnotationProcessor.findType(symtab, cu, cl.clazz));
                    }
                } else if (tree.sym.owner instanceof Symbol.MethodSymbol)
                    varTypes.put(tree.name, (Symbol.ClassSymbol) tree.sym.type.tsym);
                else if (tree.sym.owner instanceof Symbol.ClassSymbol)
                    varTypes.put(tree.name, (Symbol.ClassSymbol) tree.sym.type.tsym);

                super.visitVarDef(tree);
            }

            @Override public void visitApply(JCMethodInvocation tree) {
                if (
                    tree.meth instanceof JCFieldAccess fa &&
                    fa.selected instanceof JCIdent id &&
                    fa.name.equals(names.fromString("withConnection")) &&
                    tree.args.size() == 1 &&
                    tree.args.head instanceof JCLambda lmb
                ) {
                    var found = varTypes.get(id.name);
                    if (found != null)
                        varTypes.put(lmb.params.head.name, found);

                } else if (tree.meth instanceof JCFieldAccess f) {

                    if (
                        f.name.equals(names.fromString("fetchOne")) ||
                        f.name.equals(names.fromString("fetchOneOrNull")) ||
                        f.name.equals(names.fromString("fetchOneOptional")) ||
                        f.name.equals(names.fromString("fetchList")) ||
                        f.name.equals(names.fromString("fetchStream")) ||
                        f.name.equals(names.fromString("fetchOutParameters")) ||
                        f.name.equals(names.fromString("fetchNone")) ||
                        f.name.equals(names.fromString("fetch"))
                    ) {
                        var t = f.name.equals(names.fromString("fetchNone")) ? null :
                            resolveType(names, symtab, cu, tree.args.head);

                        boolean hasG = false;
                        if (f.selected instanceof JCFieldAccess fa) {
                            if (fa.name.equals(names.fromString("g")))
                                hasG = true;
                            else
                                throw new RuntimeException("bad pattern");
                            f = fa;
                        }
                        boolean hasWithUpdateCount = false;
                        if (f.selected instanceof JCFieldAccess fa) {
                            if (fa.name.equals(names.fromString("withUpdateCount")))
                                hasWithUpdateCount = true;
                            else
                                throw new RuntimeException("bad pattern");
                            f = fa;
                        }

                        enum DestMode {DEFAULT, CALL, GENERATED_KEYS} ;
                        var destMode = DestMode.DEFAULT;

                        if (f.selected instanceof JCMethodInvocation inv) {
                            if (inv.meth instanceof JCFieldAccess fa) {
                                if (fa.name.equals(names.fromString("asGeneratedKeys")))
                                    destMode = DestMode.GENERATED_KEYS;
                                else if (fa.name.equals(names.fromString("asCall")))
                                    destMode = DestMode.CALL;
                                else
                                    throw new RuntimeException("bad pattern");
                                f = fa;
                            } else
                                throw new RuntimeException("bad pattern");
                        }

                        JCExpression afterPrepare = null;
                        if (f.selected instanceof JCMethodInvocation inv) {
                            if (inv.meth instanceof JCFieldAccess fa) {
                                if (fa.name.equals(names.fromString("afterPrepare"))) {
                                    if (!inv.args.isEmpty())
                                        afterPrepare = inv.args.head;
                                    else
                                        throw new RuntimeException("bad pattern");
                                } else
                                    throw new RuntimeException("bad pattern");
                                f = fa;
                            } else
                                throw new RuntimeException("bad pattern");
                        }

                        final ForPrepareInvocation forPrepare;
                        final Symbol.ClassSymbol sourceType;
                        final ParsedConfiguration parsedConfig;

                        if (f.selected instanceof JCStringTemplate template) {
                            forPrepare = new ForPrepareInvocation(tree, parseQuery(symtab, cu, names, template));

                            if (template.processor instanceof JCIdent processorId) {
                                var clConnectionW = symtab.enterClass(
                                    cu.modle, names.fromString("com.truej.sql.v3.source.ConnectionW")
                                );
                                var clDataSourceW = symtab.enterClass(
                                    cu.modle, names.fromString("com.truej.sql.v3.source.DataSourceW")
                                );

                                var vt = varTypes.get(processorId.name);
                                if (
                                    vt.getInterfaces().stream().anyMatch(
                                        tt -> tt.tsym == clConnectionW || tt.tsym == clDataSourceW
                                    )
                                ) {
                                    sourceType = vt;
                                    parsedConfig = parseConfig(
                                        sourceType.className(),
                                        vt.getAnnotation(Configuration.class),
                                        false
                                    );
                                } else
                                    throw new RuntimeException("bad pattern");
                            } else
                                throw new RuntimeException("bad pattern");
                        } else
                            throw new RuntimeException("bad pattern");

                        // TODO:
                        //    1. find source & configuration
                        //    2. parse parameters (inout, out, unfold{2,3,4})
                        //    3. show compilation errors
                        //    4. parse config from environment

                        System.out.println("sourceType = " + sourceType);
                        System.out.println("parsedConfig = " + parsedConfig);
                        System.out.println("dest class = " + t);
                        System.out.println("hasG = " + hasG);
                        System.out.println("hasWithUpdateCount = " + hasWithUpdateCount);
                        System.out.println("destMode = " + destMode);
                        System.out.println("afterPrepare = " + afterPrepare);
                        System.out.println("statement = " + forPrepare);
                        System.out.println("#############");

                        if (parsedConfig.url != null) {
                            try {
                                var connection = DriverManager.getConnection(
                                    parsedConfig.url, parsedConfig.username, parsedConfig.password
                                );

                                var query = forPrepare.queryParts.stream()
                                    .map(p -> switch (p) {
                                        case InoutParameter _,
                                            OutParameter _,
                                            SimpleParameter _ -> "?";
                                        case TextPart tp -> tp.text;
                                        case UnfoldParameter u -> IntStream.range(0, u.n)
                                            .mapToObj(_ -> "?")
                                            .collect(Collectors.joining(
                                                ", ", u.n == 1 ? "" : "(", u.n == 1 ? "" : ")"
                                            ));
                                    })
                                    .collect(Collectors.joining());

                                // TODO: normalize error message from postgres ???
                                //       attach error to ast node
                                // TODO: parse GeneratedKeys


                                var stmt = switch (destMode) {
                                    case DEFAULT -> connection.prepareStatement(query);
                                    case CALL -> connection.prepareCall(query);
                                    case GENERATED_KEYS ->
                                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                };

                                // TODO:
                                // 1. check parameters - проверка должна быть отложена на уровень plugin ??? как передать эти данные на уровень плагина???
                                // 2. check resultSet against fetcher & Dto ???
                                //    find constructor with needed parameter count ???
                                //    check that arg types match ???
                                var rsMetadata = stmt.getMetaData();
                                var paramMetadata = stmt.getParameterMetaData();

                                var x = 1;

                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                super.visitApply(tree);
            }
        }.scan(tree);

        return invocations;
    }

    @Nullable String findProperty(String propertyName) {
        var prop = processingEnv.getOptions().get(propertyName);
        if (prop != null) return prop;

        prop = System.getProperty(propertyName);
        if (prop != null) return prop;

        prop = System.getenv(propertyName);
        return prop;
    }

    record ParsedConfiguration(
        String url, String username, String password, List<TypeBinding> typeBindings
    ) { }

    ParsedConfiguration parseConfig(
        String sourceClassName, Configuration sourceConfig, boolean doPrint
    ) {
        var parseProperty = (BiFunction<String, String, String>)
            (parameterName, defaultValue) -> {
                var propertyName = STR."truesql.\{sourceClassName}.\{parameterName}";
                var prop = findProperty(propertyName);

                if (prop == null)
                    if (!defaultValue.equals(Configuration.NOT_DEFINED))
                        prop = defaultValue;

                if (doPrint) System.out.println(STR."\{propertyName}=\{prop}");

                return prop;
            };

        return sourceConfig != null ?
            new ParsedConfiguration(
                parseProperty.apply("url", sourceConfig.checks().url()),
                parseProperty.apply("username", sourceConfig.checks().username()),
                parseProperty.apply("password", sourceConfig.checks().password()),
                Arrays.asList(sourceConfig.typeBindings())
            )
            : new ParsedConfiguration(null, null, null, List.of());
    }

    @Override public boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) {
        var env = (JavacProcessingEnvironment) processingEnv;
        var context = env.getContext();
        var types = Types.instance(context);

        var symtab = Symtab.instance(context);
        var trees = Trees.instance(env);
        var resolve = Resolve.instance(context);

        var names = Names.instance(context);
        var elements = JavacElements.instance(context);

        if (annotations.isEmpty()) return false;

        // FIXME: use javac messages subsystem
        if ("true".equals(findProperty("truesql.printConfig"))) {
            System.out.println("TrueSql configuration:");

            for (var element : roundEnv.getElementsAnnotatedWith(Configuration.class))
                parseConfig(
                    ((Symbol.ClassSymbol) element).className(),
                    element.getAnnotation(Configuration.class), true
                );
        }

        for (var element : roundEnv.getElementsAnnotatedWith(TrueSql.class)) {
            var found = elements.getTreeAndTopLevel(element, null, null);
            var tree = found.fst;
            var cu = found.snd;

            findFetchInvocations(names, symtab, cu, tree);
        }

        return false;
    }
}
