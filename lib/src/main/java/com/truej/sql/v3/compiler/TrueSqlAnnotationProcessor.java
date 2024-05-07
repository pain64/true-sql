package com.truej.sql.v3.compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.sun.tools.javac.tree.JCTree;

import com.sun.source.util.Trees;
import com.sun.tools.javac.util.Names;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.model.JavacElements;
import org.jetbrains.annotations.Nullable;

import static com.sun.tools.javac.tree.JCTree.*;

@SupportedAnnotationTypes("com.truej.sql.v3.TrueSql.Process")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TrueSqlAnnotationProcessor extends AbstractProcessor {

    record ImportKind(
        boolean hasM, boolean hasG, boolean hasStmt, boolean hasCall
    ) { }

    boolean isTrueSqlFullQualifiedChain(Names names, JCTree tree) {
        var comName = names.fromString("com");
        var truejName = names.fromString("truej");
        var sqlName = names.fromString("sql");
        var v3Name = names.fromString("v3");
        var trueSqlName = names.fromString("TrueSql");

        return tree instanceof JCFieldAccess f1 &&
               f1.name.equals(trueSqlName) && f1.selected instanceof JCFieldAccess f2 &&
               f2.name.equals(v3Name) && f2.selected instanceof JCFieldAccess f3 &&
               f3.name.equals(sqlName) && f3.selected instanceof JCFieldAccess f4 &&
               f4.name.equals(truejName) && f4.selected instanceof JCIdent f5 &&
               f5.name.equals(comName);
    }

    ImportKind hasGorMImport(Names names, JCCompilationUnit cu) {
        var result = new ImportKind[]{new ImportKind(false, false, false, false)};

        new TreeScanner() {
            @Override public void visitImport(JCImport anImport) {
                var starName = names.fromString("*");
                var mName = names.fromString("m");
                var gName = names.fromString("g");
                var stmtName = names.fromString("Stmt");
                var callName = names.fromString("Call");

                if (!anImport.staticImport) return;

                var hasAll = anImport.qualid.name.equals(starName);
                var hasM = hasAll || anImport.qualid.name.equals(mName);
                var hasG = hasAll || anImport.qualid.name.equals(gName);
                var hasStmt = hasAll || anImport.qualid.name.equals(stmtName);
                var hasCall = hasAll || anImport.qualid.name.equals(callName);

                if (isTrueSqlFullQualifiedChain(names, anImport.qualid.selected))
                    result[0] = new ImportKind(
                        result[0].hasM || hasM, result[0].hasG || hasG,
                        result[0].hasStmt || hasStmt, result[0].hasCall || hasCall
                    );
            }
        }.scan(cu);

        return result[0];
    }

    record MapperInvocation(
        JCTree tree, boolean generateDtoRequired, Object dtoType
    ) { }
    record ForPrepareInvocation(
        JCTree tree, String query
    ) { }
    record SafeFetchInvocation(
        JCTree forPrepare, JCTree mapper, @Nullable String[] generatedKeys
    ) { }
    // на выходе из модуля: заставить пользователя поставить аннотацию @Unsafe
    //    если нет совпадения между Stmt, m и fetch???
    // List<Task> (javac-free)
    //    1. CheckQuery (location, parsed config, query, params metadata, fields metadata)
    //    2. GenerateDto (location, fields metadata, class name)    -- patch imports ???
    //    3. GenerateMapper (location, fields metadata, class name) -- patch m calls ???
    //    4. GenerateForPrepare (location, query, params metadata)  -- patch Stmt calls ???
    //
    // NB: integration tests!
    //
    // Analysis Module -> 1. Compilation errors // <-- test cases
    //                    2. List<Task>         // <-- test cases
    // GeneratedDto Module        // <-- test cases
    // GenerateMapper Module      // <-- test cases
    // GenerateForPrepared Module // <-- test cases

    List<MapperInvocation> findMapperInvocations(
        Names names, JCCompilationUnit cu, JCTree tree
    ) {
        var invocations = new ArrayList<MapperInvocation>();
        var imports = hasGorMImport(names, cu);

        new TreeScanner() {
            @Override public void visitApply(JCMethodInvocation tree) {
                if (tree.meth instanceof JCIdent id) {
                    if (id.name.equals(names.fromString("m")) && imports.hasM)
                        invocations.add(new MapperInvocation(tree, false, null));
                    else if (id.name.equals(names.fromString("g")) && imports.hasG)
                        invocations.add(new MapperInvocation(tree, true, null));

                } else if (tree.meth instanceof JCFieldAccess field) {
                    if (isTrueSqlFullQualifiedChain(names, field.selected)) {
                        if (field.name.equals(names.fromString("m")))
                            invocations.add(new MapperInvocation(tree, false, null));
                        else if (field.name.equals(names.fromString("g")))
                            invocations.add(new MapperInvocation(tree, true, null));
                    }
                }

                super.visitApply(tree);
            }
        }.scan(tree);

        return invocations;
    }

    String makeQuery(JCStringTemplate tree) {
        return String.join("?", tree.fragments);
    }

    List<ForPrepareInvocation> findForPrepareInvocations(
        Names names, JCCompilationUnit cu, JCTree tree
    ) {
        var invocations = new ArrayList<ForPrepareInvocation>();
        var imports = hasGorMImport(names, cu);

        new TreeScanner() {
            // checks only ???
            @Override public void visitStringTemplate(JCStringTemplate tree) {
                if (tree.processor instanceof JCIdent id) {
                    if (id.name.equals(names.fromString("Stmt")) && imports.hasStmt)
                        invocations.add(new ForPrepareInvocation(tree, makeQuery(tree)));
                    else if (id.name.equals(names.fromString("Call")) && imports.hasCall)
                        invocations.add(new ForPrepareInvocation(tree, makeQuery(tree)));

                } else if (tree.processor instanceof JCFieldAccess field) {
                    if (isTrueSqlFullQualifiedChain(names, field.selected)) {
                        if (field.name.equals(names.fromString("Stmt")))
                            invocations.add(new ForPrepareInvocation(tree, makeQuery(tree)));
                        else if (field.name.equals(names.fromString("Call")))
                            invocations.add(new ForPrepareInvocation(tree, makeQuery(tree)));
                    }
                }
            }
        }.scan(tree);

        return invocations;
    }

    sealed interface GeneratedKeys { }
    record GeneratedKeysColumnNames(String[] columnNames) implements GeneratedKeys { }
    record GeneratedKeysColumnNumbers(int[] columnNumbers) implements GeneratedKeys { }

    @Nullable ForPrepareInvocation traceForPrepare(
        Names names, @Nullable GeneratedKeys generatedKeys, JCTree tree
    ) {
        return switch (tree) {
            case JCMethodInvocation invocation -> {
                if (invocation.meth instanceof JCFieldAccess field) {
                    if (field.name.equals(names.fromString("withGeneratedKeys"))) {
                        // TODO: unpack generated keys, check if already defined
                        yield traceForPrepare(names, generatedKeys, field.selected);
                    } else if (field.name.equals(names.fromString("afterPrepare"))) {
                        yield traceForPrepare(names, generatedKeys, field.selected);
                    } else if (field.name.equals(names.fromString("with"))) {
                        // require @Unsafe annotation
                        yield traceForPrepare(names, generatedKeys, field.selected);
                    } else
                        yield null;
                } else
                    yield null;
            }
            case JCStringTemplate template -> new ForPrepareInvocation(tree, makeQuery(template));
            default -> null;
        };
    }

    List<SafeFetchInvocation> findSafeFetchInvocations(Names names, JCCompilationUnit cu, JCTree tree) {
        var invocations = new ArrayList<SafeFetchInvocation>();


        new TreeScanner() {
            @Override public void visitApply(JCMethodInvocation tree) {
//  Unsafe vs Incorrect pattern ???
//      чтобы сделать m нужно знать:           -- hard error
//          type bindings => нужно знать @Configuration
//      чтобы сделать g нужно знать:           -- hard error
//          type bindings => @Configuration
//          query         => Stmt | Call
//      чтобы сделать Stmt | Call нужно знать: -- hard error
//          type bindings  => @Configuration
//          generated keys => OK PATTERN ???
//      чтобы сделать fetch нужно:             -- unsafe required
//
//  1. trace ds or connection -> nullable
//  2. trace m or g (collect to set)
//  3. trace gen keys -> Stmt | Call (collect to set)
//  4. find all m, g, Call, Stmt if not set -> hard error (cannot locate connection @Configuration)
//  5. require @Unsafe
//  6. Batch mode
//  7. Testing Api
//
//
//                Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
//                    .afterPrepare(s -> {})   -- only it once
//                    .withGeneratedKeys("id") -- only it once
//                    .fetchUpdateCount(
//                        ds, new FetcherStream<>(m(Long.class))
//                    )
//                    .fetchUpdateCount( -- allow only JCNewClass in next
//                        ds, new FetcherStream<>(someMapperXXX) -- unsafe error ???
//                    )

                Function<JCTree, String> extract = t -> {
                    return "";
                };

                if (tree.meth instanceof JCFieldAccess f) {
                    // withGeneratedKeys("id")

                    if (
                        f.name.equals(names.fromString("fetchOne")) ||
                        f.name.equals(names.fromString("fetchOneOrNull")) ||
                        f.name.equals(names.fromString("fetchOneOptional")) ||
                        f.name.equals(names.fromString("fetchList")) ||
                        f.name.equals(names.fromString("fetchStream")) ||
                        f.name.equals(names.fromString("fetchUpdateCount"))
                    ) {
                        var source = (JCIdent) tree.args.head ; // FIXME
                        new SourceConfigurationFinder().find(cu, source);

                        var forPrepare = traceForPrepare(names, null, f.selected);
                        System.out.println("for prepare = " + forPrepare);
                    }
                }
            }
        }.scan(tree);

        return invocations;
    }
    // @Autowired MainDatasource ds;
    // a.b.c.d.e.f
    // trace identifier -> local var??? | method parameter | class field | lambda parameter in withConnection
    // local var: MainDataSource ds = ... or var ds = new MainDataSource(new HikariDataSource(...))
    static class FooBar {
        int x;
        void fooBar(int x) {
            // int x;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var env = (JavacProcessingEnvironment) processingEnv;
        var context = env.getContext();
        var trees = Trees.instance(env);
        var names = Names.instance(context);
        var elements = JavacElements.instance(context);

        if (annotations.isEmpty()) return false;

        var annotatedElements
            = roundEnv.getElementsAnnotatedWith(annotations.iterator().next());

        // 1. m и g
        // 2. Stmt и Call
        // 3. fetchXXX

        for (var element : annotatedElements) {
            var found = elements.getTreeAndTopLevel(element, null, null);
            var tree = found.fst;
            var cu = found.snd;

            System.out.println(findMapperInvocations(names, cu, tree));
            System.out.println(findForPrepareInvocations(names, cu, tree));
            System.out.println(findSafeFetchInvocations(names, cu, tree));
        }

        return false;
    }
}
