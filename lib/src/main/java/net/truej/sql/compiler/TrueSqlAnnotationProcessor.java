package net.truej.sql.compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
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
import net.truej.sql.TrueSql;
import net.truej.sql.bindings.Standard;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeReadWrite;
import net.truej.sql.fetch.EvenSoNullPointerException;
import net.truej.sql.fetch.TooFewRowsException;
import net.truej.sql.fetch.TooMuchRowsException;
import net.truej.sql.fetch.UpdateResult;
import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;
import net.truej.sql.source.ParameterExtractor;
import net.truej.sql.source.Parameters;
import org.jetbrains.annotations.Nullable;

import static com.sun.tools.javac.tree.JCTree.*;
import static net.truej.sql.compiler.ExistingDtoParser.*;
import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.config.Configuration.INT_NOT_DEFINED;
import static net.truej.sql.config.Configuration.STRING_NOT_DEFINED;

@SupportedAnnotationTypes({"net.truej.sql.TrueSql", "net.truej.sql.config.Configuration"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TrueSqlAnnotationProcessor extends AbstractProcessor {


    public static Map<JCCompilationUnit, Map<JCMethodInvocation, TrueSqlPlugin.Invocation>> patchParametersTrees = new HashMap<>();

    // TODO: validate
    //     - parse existing dto to GLang - find constructor
    //     - check result set metadata against parsed
    //     - g vs not g mode, check vs no check
    //     - parse type bindings config
    //     - integrate with type bindings
    // TODO: emit the code
    // TODO: transfer ast to plugin
    // TODO: deal with error reporting

    record ForPrepareInvocation(
        JCMethodInvocation tree, QueryMode queryMode
    ) { }

    record SafeFetchInvocation( // FIXME: keep parameters
                                JCTree forPrepare, String generatedCode
    ) { }

    public sealed interface QueryPart { }

    public record TextPart(String text) implements QueryPart { }
    public record SimpleParameter(JCExpression expression) implements QueryPart { }
    public record InoutParameter(JCExpression expression) implements QueryPart { }
    public record OutParameter(Symbol.ClassSymbol toClass) implements QueryPart { }
    public record UnfoldParameter(int n, JCExpression expression) implements QueryPart { }

    sealed interface DtoMode { }
    record ExistingDto(NullMode nullMode,
                       Symbol.ClassSymbol symbol) implements DtoMode { }
    record GenerateDto(Name dtoClassName) implements DtoMode { }

    static QueryMode parseQuery(
        Symtab symtab, JCCompilationUnit cu, Names names,
        // FIXME: two nullable parameters with corellation
        JCExpression batchListDataExpression, JCLambda batchLambda,
        String queryText, com.sun.tools.javac.util.List<JCExpression> args
    ) {
        var clParameters = symtab.enterClass(
            cu.modle, names.fromString(Parameters.class.getName())
        );

        interface ParseLeaf {
            QueryPart parse(Name name, JCMethodInvocation invoke, boolean checkImport);
        }

        var map = (ParseLeaf) (name, invoke, checkImport) -> {
            var check = (Function<String, Boolean>) (sName) -> {
                if (name.equals(names.fromString(sName))) {
                    if (!checkImport) return true;

                    var imp = cu.starImportScope.findFirst(name);
                    if (imp == null)
                        imp = cu.namedImportScope.findFirst(name);

                    if (batchLambda != null) throw new RuntimeException(
                        sName + " parameter is not applicable in batch mode"
                    );

                    return imp != null &&
                           imp instanceof Symbol.MethodSymbol &&
                           imp.owner == clParameters;
                }
                return false;
            };

            if (check.apply("inout")) {
                return new InoutParameter(invoke.args.head);
            } else if (check.apply("out")) {
                var found = resolveType(names, symtab, cu, invoke.args.head);
                return new OutParameter(found);
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

        var fragments = (queryText + " ").split("(?<=[\\s,=()])\\?");
        var result = new ArrayList<QueryPart>();

        for (var i = 0; i < fragments.length; i++) {
            result.add(new TextPart(fragments[i]));

            if (i < args.size()) {
                var expression = args.get(i);
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

        return batchLambda != null
            ? new BatchedQuery(batchListDataExpression, batchLambda, result)
            : new SingleQuery(result);
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
            var found = new Symbol.ClassSymbol[]{null};
            found[0] = (Symbol.ClassSymbol) cu.toplevelScope.findFirst(id.name, isClass);

            if (found[0] == null || found[0].type.isErroneous())
                cu.accept(new TreeScanner() {
                    @Override public void visitClassDef(JCClassDecl tree) {
                        if (tree.name.equals(id.name) && tree.sym != null)
                            found[0] = tree.sym;

                        super.visitClassDef(tree);
                    }
                });

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = (Symbol.ClassSymbol) cu.namedImportScope.findFirst(id.name, isClass);

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = symtab.getClass(cu.modle, cu.packge.fullname.append('.', id.name));

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = (Symbol.ClassSymbol) cu.starImportScope.findFirst(id.name, isClass);

            if (found[0] == null || found[0].type.isErroneous())
                return null;

            return found[0];

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
        Names names, Symtab symtab, JCCompilationUnit cu, JCTree tree, String generatedClassName
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
                    var fetchMethodName = f.name.toString();

                    if (
                        f.name.equals(names.fromString("fetchOne")) ||
                        f.name.equals(names.fromString("fetchOneOrZero")) ||
                        f.name.equals(names.fromString("fetchList")) ||
                        f.name.equals(names.fromString("fetchStream")) ||
                        f.name.equals(names.fromString("fetchNone"))
                    ) {

                        var lineNumber = cu.getLineMap().getLineNumber(tree.getStartPosition());
                        final DtoMode dtoMode;

                        if (
                            f.selected instanceof JCFieldAccess fa &&
                            fa.name.equals(names.fromString("g"))
                        ) {
                            if (tree.args.isEmpty())
                                throw new RuntimeException("bad pattern");

                            if (
                                tree.args.head instanceof JCFieldAccess gfa &&
                                gfa.name.equals(names.fromString("class")) &&
                                gfa.selected instanceof JCIdent gid
                            )
                                dtoMode = new GenerateDto(gid.name);
                            else
                                throw new RuntimeException("bad pattern");

                            f = fa;
                        } else {

                            if (tree.args.isEmpty() && f.name.equals(names.fromString("fetchNone"))) {
                                dtoMode = null;
                            } else if (tree.args.size() == 1) {
                                dtoMode = new ExistingDto(
                                    NullMode.DEFAULT_NOT_NULL,
                                    resolveType(names, symtab, cu, tree.args.head)
                                );
                            } else if (tree.args.size() == 2) {
                                final NullMode nullMode;
                                if (
                                    (tree.args.head instanceof JCIdent id &&
                                     id.name.equals(names.fromString("Nullable"))) ||
                                    tree.args.head.toString().equals("net.truej.sql.source.Parameters.Nullable")
                                ) {
                                    nullMode = NullMode.EXACTLY_NULLABLE;
                                } else if (
                                    (tree.args.head instanceof JCIdent id &&
                                     id.name.equals(names.fromString("NotNull"))) ||
                                    tree.args.head.toString().equals("net.truej.sql.source.Parameters.NotNull")
                                ) {
                                    nullMode = NullMode.EXACTLY_NOT_NULL;
                                } else
                                    throw new RuntimeException("bad pattern");

                                dtoMode = f.name.equals(names.fromString("fetchNone")) ? null :
                                    new ExistingDto(
                                        nullMode, resolveType(names, symtab, cu, tree.args.get(1))
                                    );
                            } else
                                throw new RuntimeException("bad pattern");
                        }

                        boolean isWithUpdateCount = false;
                        if (f.selected instanceof JCFieldAccess fa) {
                            if (fa.name.equals(names.fromString("withUpdateCount")))
                                isWithUpdateCount = true;
                            else
                                throw new RuntimeException("bad pattern");
                            f = fa;
                        }

                        var statementMode = (StatementMode)
                            new AsDefault();

                        if (f.selected instanceof JCMethodInvocation inv) {
                            if (inv.meth instanceof JCFieldAccess fa) {

                                if (fa.name.equals(names.fromString("asGeneratedKeys"))) {
                                    if (inv.args.isEmpty())
                                        throw new RuntimeException("bad pattern");
                                    if (inv.args.getFirst() instanceof JCLiteral lit) {
                                        if (lit.getValue() instanceof Integer) {
                                            statementMode = new AsGeneratedKeysIndices(
                                                inv.args.stream().mapToInt(arg -> {
                                                    if (
                                                        arg instanceof JCLiteral al &&
                                                        al.getValue() instanceof Integer v
                                                    )
                                                        return v;
                                                    else
                                                        throw new RuntimeException("bad pattern");
                                                }).toArray()
                                            );
                                        } else if (lit.getValue() instanceof String) {
                                            statementMode = new AsGeneratedKeysColumnNames(
                                                inv.args.stream().map(arg -> {
                                                    if (
                                                        arg instanceof JCLiteral al &&
                                                        al.getValue() instanceof String v
                                                    )
                                                        return v;
                                                    else
                                                        throw new RuntimeException("bad pattern");
                                                }).toArray(String[]::new)
                                            );
                                        } else
                                            throw new RuntimeException("bad pattern");
                                    } else
                                        throw new RuntimeException("bad pattern");

                                    f = fa;
                                } else if (fa.name.equals(names.fromString("asCall"))) {
                                    statementMode = new AsCall(new int[]{});
                                    f = fa;
                                }

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
                                    f = fa;
                                }
                            } else
                                throw new RuntimeException("bad pattern");
                        }

                        final ForPrepareInvocation forPrepare;
                        final JCIdent sourceExpression;
                        final Symbol.ClassSymbol sourceType;
                        final SourceMode sourceMode;
                        final ParsedConfiguration parsedConfig;

                        if (f.selected instanceof JCMethodInvocation inv) {
                            if (inv.meth instanceof JCFieldAccess fa) {
                                if (fa.name.equals(names.fromString("q"))) {
                                    var tryAsQuery = (Function<Integer, @Nullable String>) i ->
                                        inv.args.get(i) instanceof JCLiteral lit &&
                                        lit.getValue() instanceof String text ? text : null;

                                    final QueryMode queryMode;

                                    var queryText = tryAsQuery.apply(0);
                                    if (queryText != null) // single
                                        queryMode = parseQuery(
                                            symtab, cu, names, null, null, queryText, inv.args.tail
                                        );
                                    else {
                                        queryText = tryAsQuery.apply(1);
                                        if (queryText != null) { // batch
                                            if (inv.args.size() != 3)
                                                throw new RuntimeException("bad pattern");

                                            if (
                                                inv.args.get(2) instanceof JCLambda lmb &&
                                                lmb.body instanceof JCNewArray array
                                            )
                                                queryMode = parseQuery(
                                                    symtab, cu, names, inv.args.head, lmb, queryText, array.elems
                                                );
                                            else
                                                throw new RuntimeException("bad pattern");
                                        } else
                                            throw new RuntimeException("bad pattern");
                                    }

                                    forPrepare = new ForPrepareInvocation(tree, queryMode);

                                    if (fa.selected instanceof JCIdent processorId) {
                                        sourceExpression = processorId;
                                        var clConnectionW = symtab.enterClass(
                                            cu.modle, names.fromString("net.truej.sql.source.ConnectionW")
                                        );
                                        var clDataSourceW = symtab.enterClass(
                                            cu.modle, names.fromString("net.truej.sql.source.DataSourceW")
                                        );


                                        var vt = varTypes.get(processorId.name);

                                        if (
                                            vt.getInterfaces().stream().anyMatch(
                                                tt -> tt.tsym == clConnectionW
                                            )
                                        ) {
                                            sourceMode = SourceMode.CONNECTION;
                                        } else if (
                                            vt.getInterfaces().stream().anyMatch(
                                                tt -> tt.tsym == clDataSourceW
                                            )
                                        ) {
                                            sourceMode = SourceMode.DATASOURCE;
                                        } else
                                            throw new RuntimeException("bad pattern");

                                        sourceType = vt;
                                        parsedConfig = parseConfig(
                                            symtab, names, cu,
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
                        } else
                            throw new RuntimeException("bad pattern");

                        // TODO:
                        //    1. find source & configuration
                        //    2. parse parameters (inout, out, unfold{2,3,4})
                        //    3. show compilation errors
                        //    4. parse config from environment

                        System.out.println("sourceType = " + sourceType);
                        System.out.println("parsedConfig = " + parsedConfig);
                        System.out.println("dto = " + dtoMode);
                        System.out.println("hasWithUpdateCount = " + isWithUpdateCount);
                        System.out.println("destMode = " + statementMode);
                        System.out.println("afterPrepare = " + afterPrepare);
                        System.out.println("statement = " + forPrepare);
                        System.out.println("#############");

                        var parseExistingDto = (Function<ExistingDto, FieldType>) existing ->
                            parse(
                                existing.nullMode,
                                cl -> parsedConfig.typeBindings.stream()
                                    .anyMatch(b -> b.className().equals(cl)),
                                names.init, existing.symbol
                            );

                        var getBindingForClass = (BiFunction<String, NullMode, Standard.Binding>) (
                            javaClassName, nullMode
                        ) -> {
                            var binding = parsedConfig.typeBindings.stream()
                                .filter(b ->
                                    b.className().equals(javaClassName) ||
                                    b.className().endsWith(javaClassName)
                                ).findFirst().orElseThrow(() -> new RuntimeException(
                                    "has no binding for type " + javaClassName
                                ));

                            if (
                                !binding.mayBeNullable() && nullMode == NullMode.EXACTLY_NULLABLE
                            )
                                throw new RuntimeException(
                                    "type " + javaClassName + " cannot be marked as nullable"
                                );

                            return binding;
                        };

                        interface CheckNullability {
                            void check(String fieldName, NullMode fieldNullMode, int columnIndex, ColumnMetadata column);
                        }

                        var checkNullability = (CheckNullability) (fieldName, fieldNullMode, columnIndex, column) -> {
                            enum ReportMode {WARNING, ERROR}
                            interface Reporter {
                                void report(ReportMode mode, NullMode you, NullMode driver);
                            }

                            var doReport = (Reporter) (m, you, driver) -> {
                                var message = "nullability mismatch for column " + (columnIndex + 1) +
                                              (fieldName != null ? " (for field `" + fieldName + "`) " : "") +
                                              " your decision: " + you + " driver infers: " + driver;
                                switch (m) {
                                    case WARNING:
                                        System.out.println(message);
                                        break;
                                    case ERROR:
                                        throw new RuntimeException(message);
                                }
                            };

                            switch (fieldNullMode) {
                                case EXACTLY_NULLABLE:
                                    switch (column.nullMode()) {
                                        case EXACTLY_NULLABLE,
                                             DEFAULT_NOT_NULL: { } break;
                                        case EXACTLY_NOT_NULL:
                                            doReport.report(
                                                ReportMode.WARNING,
                                                NullMode.EXACTLY_NULLABLE,
                                                NullMode.EXACTLY_NOT_NULL
                                            );
                                    }
                                    break;
                                case DEFAULT_NOT_NULL:
                                    switch (column.nullMode()) {
                                        case EXACTLY_NULLABLE:
                                            doReport.report(
                                                ReportMode.ERROR,
                                                NullMode.DEFAULT_NOT_NULL,
                                                NullMode.EXACTLY_NULLABLE
                                            );
                                            break;
                                        case DEFAULT_NOT_NULL,
                                             EXACTLY_NOT_NULL: { } break;
                                    }
                                    break;
                                case EXACTLY_NOT_NULL:
                                    switch (column.nullMode()) {
                                        case EXACTLY_NULLABLE:
                                            doReport.report(
                                                ReportMode.WARNING,
                                                NullMode.EXACTLY_NOT_NULL,
                                                NullMode.EXACTLY_NULLABLE
                                            );
                                            break;
                                        case DEFAULT_NOT_NULL,
                                             EXACTLY_NOT_NULL: { } break;
                                    }
                                    break;
                            }
                        };

                        interface CheckTypeCompatibility {
                            void check(int columnIndex, String fieldName, Standard.Binding binding, ColumnMetadata column);
                        }

                        var checkTypeCompatibility = (CheckTypeCompatibility) (columnIndex, fieldName, binding, column) -> {
                            if (
                                binding.compatibleSqlType() == null &&
                                binding.compatibleSqlTypeName() == null
                            ) {
                                if (!binding.className().equals(column.javaClassName()))
                                    throw new RuntimeException(
                                        "type mismatch for column " + (columnIndex + 1) +
                                        (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                        ". Expected " + binding.className() + " but has " + column.javaClassName()
                                    );
                            } else {
                                if (binding.compatibleSqlTypeName() != null) {
                                    if (!binding.compatibleSqlTypeName().equals(column.sqlTypeName()))
                                        throw new RuntimeException(
                                            "Sql type name mismatch for column " + (columnIndex + 1) +
                                            (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                            ". Expected " + binding.compatibleSqlTypeName() + " but has " + column.sqlTypeName()
                                        );
                                }

                                if (binding.compatibleSqlType() != null) {
                                    if (binding.compatibleSqlType() != column.sqlType())
                                        throw new RuntimeException(
                                            "Sql type id (java.sql.Types) mismatch for column " + (columnIndex + 1) +
                                            (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                            ". Expected " + binding.compatibleSqlType() + " but has " + column.sqlType()
                                        );
                                }
                            }
                        };

                        final FieldType toDto;

                        if (parsedConfig.url != null) {
                            var query = forPrepare.queryMode.parts().stream()
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

                            try (
                                var connection = DriverManager.getConnection(
                                    parsedConfig.url, parsedConfig.username, parsedConfig.password
                                );
                                var stmt = switch (statementMode) {
                                    case AsDefault _ -> connection.prepareStatement(query);
                                    case AsCall _ -> connection.prepareCall(query);
                                    case AsGeneratedKeysIndices gk -> connection.prepareStatement(
                                        query, gk.columnIndexes()
                                    );
                                    case AsGeneratedKeysColumnNames gk ->
                                        connection.prepareStatement(
                                            query, gk.columnNames()
                                        );
                                }
                            ) {

                                List<ColumnMetadata> columns;

                                switch (statementMode) {
                                    case AsDefault _,
                                         AsGeneratedKeysColumnNames _,
                                         AsGeneratedKeysIndices _ -> {

                                        if (fetchMethodName.equals("fetchNone"))
                                            columns = List.of();
                                        else {
                                            // TODO: check that metadata != null
                                            var rMetadata = stmt.getMetaData();

                                            columns = IntStream.range(1, rMetadata.getColumnCount() + 1).mapToObj(i -> {
                                                try {
                                                    return new ColumnMetadata(
                                                        switch (rMetadata.isNullable(i)) {
                                                            case ResultSetMetaData.columnNoNulls ->
                                                                NullMode.EXACTLY_NOT_NULL;
                                                            case ResultSetMetaData.columnNullable ->
                                                                NullMode.EXACTLY_NULLABLE;
                                                            case
                                                                ResultSetMetaData.columnNullableUnknown ->
                                                                NullMode.DEFAULT_NOT_NULL;
                                                            default ->
                                                                throw new IllegalStateException("unreachable");
                                                        },
                                                        rMetadata.getColumnType(i),
                                                        rMetadata.getColumnTypeName(i),
                                                        rMetadata.getColumnClassName(i),
                                                        rMetadata.getColumnLabel(i)
                                                    );
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }).toList();
                                        }
                                    }
                                    case AsCall _ -> {
                                        var pMetadata = stmt.getParameterMetaData();
                                        var parameters = forPrepare.queryMode.parts().stream()
                                            .filter(p ->
                                                !(p instanceof TextPart)
                                            ).toList();

                                        if (parameters.size() != pMetadata.getParameterCount())
                                            throw new RuntimeException("parameter count mismatch");

                                        var outParameterIndexes = IntStream.range(0, parameters.size())
                                            .peek(i -> {
                                                try {
                                                    var pMode = pMetadata.getParameterMode(i + 1);
                                                    var pModeText = switch (pMode) {
                                                        case ParameterMetaData.parameterModeIn ->
                                                            "IN";
                                                        case ParameterMetaData.parameterModeInOut ->
                                                            "INOUT";
                                                        case ParameterMetaData.parameterModeOut ->
                                                            "OUT";
                                                        case
                                                            ParameterMetaData.parameterModeUnknown ->
                                                            "UNKNOWN";
                                                        default ->
                                                            throw new IllegalStateException("unreachable");
                                                    };

                                                    switch (parameters.get(i)) {
                                                        case SimpleParameter _ -> {
                                                            if (
                                                                pMode != ParameterMetaData.parameterModeIn &&
                                                                pMode != ParameterMetaData.parameterModeUnknown
                                                            )
                                                                throw new RuntimeException(
                                                                    "For parameter " + (i + 1) +
                                                                    " mode mismatch. Expected IN but has " + pModeText
                                                                );
                                                        }
                                                        case InoutParameter _ -> {
                                                            if (
                                                                pMode != ParameterMetaData.parameterModeInOut &&
                                                                pMode != ParameterMetaData.parameterModeUnknown
                                                            )
                                                                throw new RuntimeException(
                                                                    "For parameter " + (i + 1) +
                                                                    " mode mismatch. Expected INOUT but has " + pModeText
                                                                );
                                                        }
                                                        case OutParameter _ -> {
                                                            if (
                                                                pMode != ParameterMetaData.parameterModeOut &&
                                                                pMode != ParameterMetaData.parameterModeUnknown
                                                            )
                                                                throw new RuntimeException(
                                                                    "For parameter " + (i + 1) +
                                                                    " mode mismatch. Expected OUT but has " + pModeText
                                                                );
                                                        }
                                                        default ->
                                                            throw new IllegalStateException("unreachable");
                                                    }
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                            .filter(i -> !(parameters.get(i) instanceof SimpleParameter))
                                            .map(i -> i + 1)
                                            .toArray();

                                        columns = Arrays.stream(outParameterIndexes).mapToObj(i -> {
                                            try {
                                                return new ColumnMetadata(
                                                    switch (pMetadata.isNullable(i)) {
                                                        case ParameterMetaData.parameterNoNulls ->
                                                            NullMode.EXACTLY_NOT_NULL;
                                                        case ParameterMetaData.parameterNullable ->
                                                            NullMode.EXACTLY_NULLABLE;
                                                        case
                                                            ParameterMetaData.parameterNullableUnknown ->
                                                            NullMode.DEFAULT_NOT_NULL;
                                                        default ->
                                                            throw new IllegalStateException("unreachable");
                                                    },
                                                    pMetadata.getParameterType(i),
                                                    pMetadata.getParameterTypeName(i),
                                                    pMetadata.getParameterClassName(i),
                                                    "a" + i
                                                );
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }).toList();

                                        statementMode = new AsCall(outParameterIndexes);
                                    }
                                }

                                toDto = switch (dtoMode) {
                                    case null -> null;
                                    case ExistingDto existing -> {
                                        var parsed = parseExistingDto.apply(existing);
                                        var dtoFields = flattedDtoType(parsed);

                                        if (dtoFields.size() != columns.size())
                                            throw new RuntimeException(
                                                "target type implies " + dtoFields.size() + " columns but result has "
                                                + columns.size()
                                            );

                                        for (var i = 0; i < dtoFields.size(); i++) {
                                            var field = dtoFields.get(i);
                                            var column = columns.get(i);

                                            // FIXME: deduplicate
                                            var binding = getBindingForClass.apply(
                                                field.st().javaClassName(), field.st().nullMode()
                                            );

                                            checkNullability.check(
                                                field.name(), field.st().nullMode(), i, column
                                            );

                                            checkTypeCompatibility.check(
                                                i, field.name(), binding, column
                                            );
                                        }

                                        yield parsed;
                                    }
                                    case GenerateDto gDto -> {
                                        var fields = parseResultSetColumns(
                                            columns, (column, columnIndex, javaClassNameHint, dtoNullMode) -> {

                                                // FIXME: deduplicate

                                                var binding = getBindingForClass.apply(
                                                    javaClassNameHint != null ? javaClassNameHint : column.javaClassName(),
                                                    dtoNullMode
                                                );

                                                checkNullability.check(
                                                    column.columnName(), dtoNullMode, columnIndex, column
                                                );

                                                checkTypeCompatibility.check(
                                                    columnIndex, column.columnName(), binding, column
                                                );

                                                return binding.className();
                                            }
                                        );

                                        yield new AggregatedType(gDto.dtoClassName.toString(), fields);
                                    }
                                };

                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else { // no compile time checks
                            toDto = switch (dtoMode) {
                                case null -> null;
                                case ExistingDto existing -> {
                                    var parsed = parseExistingDto.apply(existing);
                                    var dtoFields = flattedDtoType(parsed);

                                    for (var field : dtoFields) {
                                        var _ = getBindingForClass.apply(
                                            field.st().javaClassName(), field.st().nullMode()
                                        );
                                    }

                                    if (statementMode instanceof AsCall) {
                                        var parameters = forPrepare.queryMode.parts().stream()
                                            .filter(p ->
                                                !(p instanceof TextPart)
                                            ).toList();

                                        var outParameterIndexes = IntStream.range(0, parameters.size())
                                            .filter(i -> !(parameters.get(i) instanceof SimpleParameter))
                                            .map(i -> i + 1)
                                            .toArray();

                                        if (outParameterIndexes.length != dtoFields.size())
                                            throw new RuntimeException(
                                                "expected " + dtoFields.size() + " columns for dto but has " +
                                                outParameterIndexes.length + " INOUT/OUT parameters"
                                            );
                                    }

                                    yield parsed;
                                }
                                case GenerateDto _ -> throw new RuntimeException(
                                    "compile time checks must be enabled for .g"
                                );
                            };
                        }

                        var fetchMode = switch (fetchMethodName) {
                            case "fetchOne" -> new FetchOne(toDto);
                            case "fetchOneOrZero" -> new FetchOneOrZero(toDto);
                            case "fetchList" -> new FetchList(toDto);
                            case "fetchStream" -> new FetchStream(toDto);
                            case "fetchNone" -> new FetchNone();
                            default -> throw new RuntimeException("unreachable");
                        };

                        var generatedCode = generate(
                            (className) ->
                                parsedConfig.typeBindings.stream()
                                    .filter(b -> b.className().equals(className))
                                    .findFirst().orElseThrow(() -> new IllegalStateException("unreachable"))
                                    .rwClassName(),
                            lineNumber,
                            sourceMode,
                            forPrepare.queryMode,
                            statementMode,
                            fetchMode,
                            dtoMode instanceof GenerateDto,
                            isWithUpdateCount
                        );

                        patchParametersTrees
                            .computeIfAbsent(cu, _ -> new HashMap<>())
                            .put(forPrepare.tree, new TrueSqlPlugin.Invocation(
                                parsedConfig.typeBindings, generatedClassName, fetchMethodName, lineNumber,
                                sourceExpression, forPrepare.queryMode, null
                            ));

                        invocations.add(new SafeFetchInvocation(
                            // FIXME
                            null, generatedCode
                        ));
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
        String url, String username, String password, List<Standard.Binding> typeBindings
    ) { }

    ParsedConfiguration parseConfig(
        Symtab symtab, Names names, JCCompilationUnit cu, String sourceClassName, Configuration sourceConfig, boolean doPrint
    ) {
        var parseProperty = (BiFunction<String, String, String>)
            (parameterName, defaultValue) -> {
                var propertyName = STR."truesql.\{sourceClassName}.\{parameterName}";
                var prop = findProperty(propertyName);

                if (prop == null)
                    if (!defaultValue.equals(Configuration.STRING_NOT_DEFINED))
                        prop = defaultValue;

                if (doPrint) System.out.println(STR."\{propertyName}=\{prop}");

                return prop;
            };

        if (sourceConfig != null) {
            var bindings = new ArrayList<>(Standard.bindings);

            for (var tb : sourceConfig.typeBindings()) {
                Type.ClassType rwClassType;

                try {
                    rwClassType =
                        new Type.ClassType(
                            null, com.sun.tools.javac.util.List.nil(),
                            symtab.getClass(cu.modle, names.fromString(tb.rw().getName()))
                        );
                } catch (MirroredTypeException mte) {
                    rwClassType = ((Type.ClassType) mte.getTypeMirror());
                }

                var bound = BoundTypeExtractor.extract(
                    symtab.getClass(cu.modle, names.fromString(TypeReadWrite.class.getName())),
                    rwClassType
                );

                var emptyArgsConstructor = ((Symbol.ClassSymbol) rwClassType.tsym).members_field.getSymbols(sym ->
                    sym instanceof Symbol.MethodSymbol m && m.name.equals(names.fromString("<init>")) && m.params.isEmpty()
                ).iterator();

                if (!emptyArgsConstructor.hasNext())
                    throw new RuntimeException(
                        "rw class " + rwClassType.tsym.flatName().toString() + " has no no-arg constructor"
                    );


                bindings.add(new Standard.Binding(
                    bound.tsym.flatName().toString(),
                    rwClassType.tsym.flatName().toString(),
                    tb.mayBeNullable(),
                    tb.compatibleSqlType() != INT_NOT_DEFINED ? tb.compatibleSqlType() : null,
                    !tb.compatibleSqlTypeName().equals(STRING_NOT_DEFINED) ? tb.compatibleSqlTypeName() : null
                ));
            }

            return new ParsedConfiguration(
                parseProperty.apply("url", sourceConfig.checks().url()),
                parseProperty.apply("username", sourceConfig.checks().username()),
                parseProperty.apply("password", sourceConfig.checks().password()),
                bindings
            );
        } else
            return new ParsedConfiguration(null, null, null, Standard.bindings);
    }

    @Override public boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) {
        System.out.println("annotation processor started!!!");

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

            for (var element : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
                var found = elements.getTreeAndTopLevel(element, null, null);
                var cu = found.snd;
                parseConfig(
                    symtab, names, cu,
                    ((Symbol.ClassSymbol) element).className(),
                    element.getAnnotation(Configuration.class), true
                );
            }
        }

        for (var element : roundEnv.getElementsAnnotatedWith(TrueSql.class)) {
            var found = elements.getTreeAndTopLevel(element, null, null);
            var tree = found.fst;
            var cu = found.snd;
            var elementSymbol = (Symbol.ClassSymbol) element;

            var generatedClassName = (elementSymbol).getQualifiedName() + "TrueSql";
            var invocations = findFetchInvocations(names, symtab, cu, tree, generatedClassName);

            try {
                var builderFile = env.getFiler().createSourceFile(
                    generatedClassName, element
                );

                try (var out = new PrintWriter(builderFile.openWriter())) {
                    out.write("package " +
                              elementSymbol.getQualifiedName().toString()
                                  .replace("." + elementSymbol.getSimpleName().toString(), "") +
                              ";\n"
                    );
                    out.write("import " + TypeReadWrite.class.getName() + ";\n");
                    out.write("import " + ConnectionW.class.getName() + ";\n");
                    out.write("import " + DataSourceW.class.getName() + ";\n");
                    out.write("import " + TooMuchRowsException.class.getName() + ";\n");
                    out.write("import " + TooFewRowsException.class.getName() + ";\n");
                    out.write("import " + UpdateResult.class.getName() + ";\n");
                    out.write("import " + ParameterExtractor.class.getName() + ";\n");
                    out.write("import " + Parameters.class.getName() + ".*;\n");
                    out.write("import " + EvenSoNullPointerException.class.getName() + ";\n");
                    out.write("import net.truej.sql.bindings.*;\n");
                    out.write("import java.util.List;\n");
                    out.write("import java.util.stream.Stream;\n");
                    out.write("import java.util.stream.Collectors;\n");
                    out.write("import java.util.Objects;\n");
                    out.write("import java.sql.SQLException;\n");
                    // out.write("import jstack.greact.SafeSqlPlugin.Depends;\n\n");
                    // out.write("@Depends(%s.class)\n".formatted(elementSymbol.getQualifiedName()));
                    out.write("class %sTrueSql { \n".formatted(elementSymbol.getSimpleName()));

                    for (var invocation : invocations)
                        out.write(invocation.generatedCode);
                    out.write("}");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
