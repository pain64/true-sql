package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysColumnNames;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysIndices;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.Parameters;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.truej.sql.compiler.ExistingDtoParser.flattedDtoType;
import static net.truej.sql.compiler.ExistingDtoParser.parse;
import static net.truej.sql.compiler.GLangParser.parseResultSetColumns;
import static net.truej.sql.compiler.StatementGenerator.generate;
import static net.truej.sql.compiler.StatementGenerator.unfoldArgumentsCount;
import static net.truej.sql.compiler.TrueSqlPlugin.doofyEncode;

public class InvocationsFinder {
    private static final String POSTGRESQL_DB_NAME = "PostgreSQL";
    private static final String HSQL_DB_NAME = "HSQL Database Engine";
    private static final String MYSQL_DB_NAME = "MySQL";
    private static final String MARIA_DB_NAME = "MariaDB";
    private static final String MSSQL_DB_NAME = "Microsoft SQL Server";
    private static final String ORACLE_DB_NAME = "Oracle";

    public sealed interface QueryPart { }
    public record TextPart(String text) implements QueryPart { }
    public record SimpleParameter(JCTree.JCExpression expression) implements QueryPart { }
    public record InoutParameter(JCTree.JCExpression expression) implements QueryPart { }
    public record OutParameter(Symbol.ClassSymbol toClass) implements QueryPart { }
    public record UnfoldParameter(
        JCTree.JCExpression expression, @Nullable JCTree.JCLambda extractor
    ) implements QueryPart { }

    // FIXME: remove ???
    record ForPrepareInvocation(
        JCTree.JCMethodInvocation tree, StatementGenerator.QueryMode queryMode
    ) { }

    record ExistingDto(GLangParser.NullMode nullMode,
                       Symbol.ClassSymbol symbol) implements DtoMode { }
    record GenerateDto(Name dtoClassName) implements DtoMode { }

    sealed interface DtoMode { }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }

    static StatementGenerator.QueryMode parseQuery(
        Symtab symtab, JCTree.JCCompilationUnit cu, Names names,
        // FIXME: two nullable parameters with corellation
        JCTree.JCExpression batchListDataExpression, JCTree.JCLambda batchLambda,
        String queryText, com.sun.tools.javac.util.List<JCTree.JCExpression> args
    ) {
        var clParameters = symtab.enterClass(
            cu.modle, names.fromString(Parameters.class.getName())
        );

        interface ParseLeaf {
            QueryPart parse(Name name, JCTree.JCMethodInvocation invoke, boolean checkImport);
        }

        var map = (ParseLeaf) (name, invoke, checkImport) -> {
            var check = (Function<String, Boolean>) (sName) -> {
                if (name.equals(names.fromString(sName))) {
                    if (!checkImport) return true;

                    var imp = cu.starImportScope.findFirst(name);
                    if (imp == null)
                        imp = cu.namedImportScope.findFirst(name);

                    if (batchLambda != null) throw new ValidationException(
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
                var found = TypeFinder.resolve(names, symtab, cu, invoke.args.head);
                return new OutParameter(found);
            } else if (check.apply("unfold")) {
                if (invoke.args.size() == 1)
                    return new UnfoldParameter(invoke.args.head, null);
                else if (invoke.args.size() == 2) {
                    if (
                        invoke.args.get(1) instanceof JCTree.JCLambda lmb &&
                        lmb.body instanceof JCTree.JCNewArray array &&
                        array.elemtype instanceof JCTree.JCIdent elementTypeId &&
                        elementTypeId.name.equals(names.fromString("Object"))
                    )
                        return new UnfoldParameter(invoke.args.head, lmb);
                    else
                        throw new ValidationException(
                            "Unfold parameter extractor must be lambda literal returning " +
                            "object array literal (e.g. `u -> new Object[]{u.f1, u.f2}`)"
                        );
                } else
                    throw new ValidationException("bad tree"); // FIXME: refactor to new bad tree subsystem
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

                if (expression instanceof JCTree.JCMethodInvocation invoke) {
                    if (invoke.meth instanceof JCTree.JCIdent id) {
                        parsed = map.parse(id.name, invoke, true);
                    } else if (
                        invoke.meth instanceof JCTree.JCFieldAccess fa &&
                        fa.selected instanceof JCTree.JCIdent id &&
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
            ? new StatementGenerator.BatchedQuery(batchListDataExpression, batchLambda, result)
            : new StatementGenerator.SingleQuery(result);
    }

    static List<String> find(
        ProcessingEnvironment env, Context context, Symtab symtab, Names names,
        TreeMaker maker, CompilerMessages messages, JCTree.JCCompilationUnit cu, JCTree tree,
        String generatedClassName
    ) {

        var result = new ArrayList<String>();

        new TreeScanner() {
            final Map<Name, Symbol.ClassSymbol> varTypes = new HashMap<>();
            final Set<Name> withConnectionSource = new HashSet<>();

            @Override public void visitVarDef(JCTree.JCVariableDecl tree) {
                if (tree.sym == null) {
                    if (tree.vartype != null)
                        varTypes.put(tree.name, TypeFinder.find(symtab, cu, tree.vartype));
                    else {
                        if (tree.init instanceof JCTree.JCNewClass cl)
                            varTypes.put(tree.name, TypeFinder.find(symtab, cu, cl.clazz));
                    }
                } else if (
                    tree.sym.owner instanceof Symbol.MethodSymbol &&
                    tree.sym.type.tsym instanceof Symbol.ClassSymbol vTypeSym
                )
                    varTypes.put(tree.name, vTypeSym);
                else if (
                    tree.sym.owner instanceof Symbol.ClassSymbol &&
                    tree.sym.type.tsym instanceof Symbol.ClassSymbol vTypeSym
                )
                    varTypes.put(tree.name, vTypeSym);

                super.visitVarDef(tree);
            }

            void handleInvocation(JCTree.JCMethodInvocation tree) {
                var zz = 1;
                if (
                    tree.meth instanceof JCTree.JCFieldAccess fa &&
                    fa.selected instanceof JCTree.JCIdent id && (
                        fa.name.equals(names.fromString("withConnection")) ||
                        fa.name.equals(names.fromString("inTransaction"))
                    ) &&
                    tree.args.size() == 1 &&
                    tree.args.head instanceof JCTree.JCLambda lmb &&
                    lmb.params.size() == 1
                ) {
                    var found = varTypes.get(id.name);
                    if (found != null) {
                        var cnParameterName = lmb.params.head.name;
                        varTypes.put(cnParameterName, found);
                        withConnectionSource.add(cnParameterName);
                    }
                } else if (
                    tree.meth instanceof JCTree.JCFieldAccess fa &&
                    fa.name.equals(names.fromString("constraint"))
                ) {
                    if (tree.args.length() < 3 || tree.args.length() > 5) return;
                    var sourceExpression = fa.selected;

                    if (sourceExpression instanceof JCTree.JCIdent idExpr) {
                        var vt = varTypes.get(idExpr.name);

                        var parsedConfig = ConfigurationParser.parseConfig(
                            env, symtab, names, cu,
                            vt.className(),
                            vt.getAnnotation(Configuration.class),
                            false
                        );

                        record CatalogAndSchema(String catalogName, String schemaName) { }

                        try (
                            var connection = DriverManager.getConnection(
                                parsedConfig.url(), parsedConfig.username(), parsedConfig.password()
                            )
                        ) {
                            var onDatabase = connection.getMetaData().getDatabaseProductName();

                            var findConstraint = (
                                BiFunction<String, List<String>, CatalogAndSchema>
                                ) (query, args) -> {

                                try {
                                    var stmt = connection.prepareStatement(query);
                                    for (var i = 0; i < args.size(); i++)
                                        stmt.setString(i + 1, args.get(i));

                                    var rs = stmt.executeQuery();
                                    if (!rs.next())
                                        throw new ValidationException(
                                            "constraint not found"
                                        );

                                    var full = new CatalogAndSchema(
                                        rs.getString(1), rs.getString(2)
                                    );

                                    if (rs.next()) throw new ValidationException(
                                        "too much constraints for criteria"
                                    );

                                    return full;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            };

                            var defaultQuery = """
                                select
                                    '', ''
                                from information_schema.table_constraints
                                where
                                    upper(TABLE_CATALOG)   = upper(?) and
                                    upper(TABLE_SCHEMA)    = upper(?) and
                                    upper(TABLE_NAME)      = upper(?) and
                                    upper(CONSTRAINT_NAME) = upper(?)""";

                            if (onDatabase.equals(MYSQL_DB_NAME))
                                defaultQuery = """
                                    select
                                        '', ?
                                    from information_schema.table_constraints
                                    where
                                        upper(TABLE_SCHEMA)    = upper(?) and
                                        upper(TABLE_NAME)      = upper(?) and
                                        upper(CONSTRAINT_NAME) = upper(?)
                                    """;

                            if (onDatabase.equals(ORACLE_DB_NAME))
                                defaultQuery = """
                                    select
                                        '', ?
                                    from all_constraints
                                    where
                                        upper(OWNER)           = upper(?) and
                                        upper(TABLE_NAME)      = upper(?) and
                                        upper(CONSTRAINT_NAME) = upper(?)
                                    """;

                            switch (tree.args.length()) {
                                case 3:
                                    if (
                                        tree.args.get(0) instanceof JCTree.JCLiteral l1 &&
                                        l1.getValue() instanceof String tableName &&
                                        tree.args.get(1) instanceof JCTree.JCLiteral l2 &&
                                        l2.getValue() instanceof String constraintName
                                    ) {
                                        var rsToString = (Function<ResultSet, String>) rs ->
                                            Stream.iterate(
                                                rs, t -> {
                                                    try {
                                                        return t.next();
                                                    } catch (SQLException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }, t -> t
                                            ).map(r -> {
                                                try {
                                                    var s = "";
                                                    for (var i = 0; i < r.getMetaData().getColumnCount(); i++)
                                                        s += r.getMetaData().getColumnLabel(i + 1) + "=" + r.getObject(i + 1) + ";";
                                                    return s;

                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }).collect(Collectors.joining("\n"));

                                        var realCatalog = connection.getCatalog();
                                        var realSchema = connection.getSchema();

                                        // FIXME: correct resolve for PostgreSQL (search_path)

                                        if (onDatabase.equals(MYSQL_DB_NAME)) {
                                            realCatalog = "def";
                                            realSchema = connection.getCatalog();
                                        }

                                        if (onDatabase.equals(ORACLE_DB_NAME)) {
                                            realCatalog = "";
                                        }

                                        var _ = findConstraint.apply(
                                            defaultQuery, List.of(
                                                realCatalog, realSchema,
                                                tableName, constraintName
                                            )
                                        );

                                        tree.args = com.sun.tools.javac.util.List.<JCTree.JCExpression>of(
                                                maker.Literal(realCatalog)
                                            )
                                            .append(maker.Literal(realSchema))
                                            .appendList(tree.args);

                                    } else
                                        throw new RuntimeException("bad tree");
                                    break;
                                case 4:
                                    if (
                                        tree.args.get(0) instanceof JCTree.JCLiteral l1 &&
                                        l1.getValue() instanceof String schemaName &&
                                        tree.args.get(1) instanceof JCTree.JCLiteral l2 &&
                                        l2.getValue() instanceof String tableName &&
                                        tree.args.get(2) instanceof JCTree.JCLiteral l3 &&
                                        l3.getValue() instanceof String constraintName
                                    ) {
                                        var realCatalog = connection.getCatalog();

                                        if (onDatabase.equals(ORACLE_DB_NAME))
                                            realCatalog = ""; // unused

                                        var _ = findConstraint.apply(
                                            defaultQuery, List.of(
                                                realCatalog, schemaName,
                                                tableName, constraintName
                                            )
                                        );

                                        tree.args = com.sun.tools.javac.util.List.<JCTree.JCExpression>of(
                                                maker.Literal(realCatalog)
                                            )
                                            .appendList(tree.args);

                                    } else throw new RuntimeException("bad tree");
                                    break;
                                default:  // 5
                                    if (
                                        tree.args.get(0) instanceof JCTree.JCLiteral l1 &&
                                        l1.getValue() instanceof String catalogName &&
                                        tree.args.get(1) instanceof JCTree.JCLiteral l2 &&
                                        l2.getValue() instanceof String schemaName &&
                                        tree.args.get(2) instanceof JCTree.JCLiteral l3 &&
                                        l3.getValue() instanceof String tableName &&
                                        tree.args.get(3) instanceof JCTree.JCLiteral l4 &&
                                        l4.getValue() instanceof String constraintName
                                    ) {
                                        // TODO: not supported in oracle
                                        var _ = findConstraint.apply(
                                            defaultQuery, List.of(
                                                catalogName, schemaName, tableName, constraintName
                                            )
                                        );

                                    } else
                                        throw new RuntimeException("bad tree");
                                    break;
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // bad tree
                } else if (tree.meth instanceof JCTree.JCFieldAccess f) {
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
                            f.selected instanceof JCTree.JCFieldAccess fa &&
                            fa.name.equals(names.fromString("g"))
                        ) {
                            if (tree.args.isEmpty()) return;

                            if (
                                tree.args.head instanceof JCTree.JCFieldAccess gfa &&
                                gfa.name.equals(names.fromString("class")) &&
                                gfa.selected instanceof JCTree.JCIdent gid
                            )
                                dtoMode = new GenerateDto(gid.name);
                            else
                                return; // TODO: check that .class literal

                            f = fa;
                        } else {

                            if (tree.args.isEmpty() && f.name.equals(names.fromString("fetchNone"))) {
                                dtoMode = null;
                            } else if (tree.args.size() == 1) {
                                dtoMode = new ExistingDto(
                                    GLangParser.NullMode.DEFAULT_NOT_NULL,
                                    TypeFinder.resolve(names, symtab, cu, tree.args.head)
                                );
                            } else if (tree.args.size() == 2) {
                                final GLangParser.NullMode nullMode;
                                if (
                                    (tree.args.head instanceof JCTree.JCIdent id &&
                                     id.name.equals(names.fromString("Nullable"))) ||
                                    tree.args.head.toString().equals("net.truej.sql.source.Parameters.Nullable")
                                ) {
                                    nullMode = GLangParser.NullMode.EXACTLY_NULLABLE;
                                } else if (
                                    (tree.args.head instanceof JCTree.JCIdent id &&
                                     id.name.equals(names.fromString("NotNull"))) ||
                                    tree.args.head.toString().equals("net.truej.sql.source.Parameters.NotNull")
                                ) {
                                    nullMode = GLangParser.NullMode.EXACTLY_NOT_NULL;
                                } else
                                    return;

                                dtoMode = f.name.equals(names.fromString("fetchNone")) ? null :
                                    new ExistingDto(
                                        nullMode, TypeFinder.resolve(names, symtab, cu, tree.args.get(1))
                                    );
                            } else
                                return;
                        }

                        boolean isWithUpdateCount = false;
                        if (f.selected instanceof JCTree.JCFieldAccess fa) {
                            if (fa.name.equals(names.fromString("withUpdateCount")))
                                isWithUpdateCount = true;
                            else
                                return;
                            f = fa;
                        }

                        var statementMode = (StatementGenerator.StatementMode)
                            new StatementGenerator.AsDefault();

                        if (f.selected instanceof JCTree.JCMethodInvocation inv) {
                            if (inv.meth instanceof JCTree.JCFieldAccess fa) {

                                if (fa.name.equals(names.fromString("asGeneratedKeys"))) {
                                    if (inv.args.isEmpty())
                                        return;
                                    if (inv.args.getFirst() instanceof JCTree.JCLiteral lit) {
                                        if (lit.getValue() instanceof Integer) {

                                            if (!inv.args.stream().allMatch(
                                                arg -> arg instanceof JCTree.JCLiteral al &&
                                                       al.getValue() instanceof Integer
                                            )) return;

                                            statementMode = new AsGeneratedKeysIndices(

                                                inv.args.stream().mapToInt(arg ->
                                                    (Integer) ((JCTree.JCLiteral) arg).value
                                                ).toArray()
                                            );
                                        } else if (lit.getValue() instanceof String) {
                                            if (!inv.args.stream().allMatch(arg ->
                                                arg instanceof JCTree.JCLiteral al &&
                                                al.getValue() instanceof String
                                            )) return;

                                            statementMode = new AsGeneratedKeysColumnNames(
                                                inv.args.stream().map(arg ->
                                                    (String) ((JCTree.JCLiteral) arg).value
                                                ).toArray(String[]::new)
                                            );
                                        } else
                                            return;
                                    } else
                                        return;

                                    f = fa;
                                } else if (fa.name.equals(names.fromString("asCall"))) {
                                    statementMode = new StatementGenerator.AsCall(new int[]{});
                                    f = fa;
                                }

                            } else
                                return;
                        }

                        JCTree.JCExpression afterPrepare = null;
                        if (f.selected instanceof JCTree.JCMethodInvocation inv) {
                            if (inv.meth instanceof JCTree.JCFieldAccess fa) {
                                if (fa.name.equals(names.fromString("afterPrepare"))) {
                                    if (!inv.args.isEmpty())
                                        afterPrepare = inv.args.head;
                                    else
                                        return;
                                    f = fa;
                                }
                            } else
                                return;
                        }

                        final ForPrepareInvocation forPrepare;
                        final JCTree.JCIdent sourceExpression;
                        final Symbol.ClassSymbol sourceType;
                        final StatementGenerator.SourceMode sourceMode;
                        final ConfigurationParser.ParsedConfiguration parsedConfig;

                        if (f.selected instanceof JCTree.JCMethodInvocation inv) {
                            if (inv.meth instanceof JCTree.JCFieldAccess fa) {
                                if (fa.name.equals(names.fromString("q"))) {
                                    var tryAsQuery = (Function<Integer, @Nullable String>) i ->
                                        i < inv.args.length() &&
                                        inv.args.get(i) instanceof JCTree.JCLiteral lit &&
                                        lit.getValue() instanceof String text ? text : null;

                                    final StatementGenerator.QueryMode queryMode;

                                    var queryText = tryAsQuery.apply(0);
                                    if (queryText != null) // single
                                        queryMode = parseQuery(
                                            symtab, cu, names, null, null, queryText, inv.args.tail
                                        );
                                    else {
                                        queryText = tryAsQuery.apply(1);
                                        if (queryText != null) { // batch
                                            if (inv.args.size() != 3)
                                                return;

                                            if (
                                                inv.args.get(2) instanceof JCTree.JCLambda lmb &&
                                                lmb.body instanceof JCTree.JCNewArray array &&
                                                array.elemtype instanceof JCTree.JCIdent elementTypeId &&
                                                elementTypeId.name.equals(names.fromString("Object"))
                                            )
                                                queryMode = parseQuery(
                                                    symtab, cu, names, inv.args.head, lmb, queryText, array.elems
                                                );
                                            else
                                                return;
                                        } else
                                            return;

                                    }

                                    forPrepare = new ForPrepareInvocation(tree, queryMode);

                                    if (fa.selected instanceof JCTree.JCIdent processorId) {
                                        sourceExpression = processorId;
                                        var clConnectionW = symtab.enterClass(
                                            cu.modle, names.fromString("net.truej.sql.source.ConnectionW")
                                        );
                                        var clDataSourceW = symtab.enterClass(
                                            cu.modle, names.fromString("net.truej.sql.source.DataSourceW")
                                        );


                                        var vt = varTypes.get(processorId.name);

                                        if (vt.getSuperclass().tsym == clConnectionW) {
                                            sourceMode = StatementGenerator.SourceMode.CONNECTION;
                                        } else if (vt.getSuperclass().tsym == clDataSourceW) {
                                            if (withConnectionSource.contains(processorId.name))
                                                sourceMode = StatementGenerator.SourceMode.CONNECTION;
                                            else
                                                sourceMode = StatementGenerator.SourceMode.DATASOURCE;
                                        } else
                                            return;


                                        sourceType = vt;
                                        parsedConfig = ConfigurationParser.parseConfig(
                                            env, symtab, names, cu,
                                            sourceType.className(),
                                            vt.getAnnotation(Configuration.class),
                                            false
                                        );

                                    } else return;
                                } else return;
                            } else return;
                        } else return;

                        var parseExistingDto = (Function<ExistingDto, GLangParser.FieldType>) existing ->
                            parse(
                                existing.nullMode,
                                cl -> parsedConfig.typeBindings().stream()
                                    .anyMatch(b -> b.className().equals(cl)),
                                names.init, existing.symbol
                            );

                        var getBindingForClass = (BiFunction<String, GLangParser.NullMode, Standard.Binding>) (
                            javaClassName, nullMode
                        ) -> {
                            var binding = parsedConfig.typeBindings().stream()
                                .filter(b ->
                                    b.className().equals(javaClassName) ||
                                    b.className().endsWith(javaClassName)
                                ).findFirst().orElseThrow(() -> new ValidationException(
                                    "has no binding for type " + javaClassName
                                ));

                            if (
                                !binding.mayBeNullable() && nullMode == GLangParser.NullMode.EXACTLY_NULLABLE
                            )
                                throw new ValidationException(
                                    "type " + javaClassName + " cannot be marked as nullable"
                                );

                            return binding;
                        };

                        interface CheckNullability {
                            void check(String fieldName, GLangParser.NullMode fieldNullMode, int columnIndex, GLangParser.ColumnMetadata column);
                        }

                        var checkNullability = (CheckNullability) (fieldName, fieldNullMode, columnIndex, column) -> {
                            enum ReportMode {WARNING, ERROR}
                            interface Reporter {
                                void report(ReportMode mode, GLangParser.NullMode you, GLangParser.NullMode driver);
                            }

                            var doReport = (Reporter) (m, you, driver) -> {
                                var message = "nullability mismatch for column " + (columnIndex + 1) +
                                              (fieldName != null ? " (for field `" + fieldName + "`) " : "") +
                                              " your decision: " + you + " driver infers: " + driver;
                                switch (m) {
                                    case WARNING:
                                        messages.write(
                                            cu, forPrepare.tree, JCDiagnostic.DiagnosticType.WARNING, message
                                        );
                                        System.out.println(message);
                                        break;
                                    case ERROR:
                                        throw new ValidationException(message);
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
                                                GLangParser.NullMode.EXACTLY_NULLABLE,
                                                GLangParser.NullMode.EXACTLY_NOT_NULL
                                            );
                                    }
                                    break;
                                case DEFAULT_NOT_NULL:
                                    switch (column.nullMode()) {
                                        case EXACTLY_NULLABLE:
                                            doReport.report(
                                                ReportMode.ERROR,
                                                GLangParser.NullMode.DEFAULT_NOT_NULL,
                                                GLangParser.NullMode.EXACTLY_NULLABLE
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
                                                GLangParser.NullMode.EXACTLY_NOT_NULL,
                                                GLangParser.NullMode.EXACTLY_NULLABLE
                                            );
                                            break;
                                        case DEFAULT_NOT_NULL,
                                             EXACTLY_NOT_NULL: { } break;
                                    }
                                    break;
                            }
                        };

                        interface CheckTypeCompatibility {
                            void check(
                                String onDatabase,
                                int columnIndex,
                                String fieldName,
                                Standard.Binding binding,
                                GLangParser.NullMode bindingNullMode,
                                GLangParser.ColumnMetadata column
                            );
                        }

                        var checkTypeCompatibility = (CheckTypeCompatibility) (
                            onDatabase, columnIndex, fieldName, toBinding, bindingNullMode, fromColumn
                        ) -> {


                            if ((
                                onDatabase.equals(MYSQL_DB_NAME) ||
                                onDatabase.equals(MARIA_DB_NAME)
                            )) {
                                if (
                                    fromColumn.sqlType() == Types.BIGINT && (
                                        toBinding.className().equals(Long.class.getName()) ||
                                        toBinding.className().equals(long.class.getName())
                                    )
                                ) return;
                            }

                            if (onDatabase.equals(ORACLE_DB_NAME)) {
                                if (
                                    fromColumn.sqlTypeName().equals("NUMBER") &&
                                    fromColumn.scale() == 0 && (
                                        toBinding.className().equals(Long.class.getName()) ||
                                        toBinding.className().equals(long.class.getName()) ||
                                        toBinding.className().equals(Integer.class.getName()) ||
                                        toBinding.className().equals(int.class.getName()) ||
                                        toBinding.className().equals(Short.class.getName()) ||
                                        toBinding.className().equals(short.class.getName()) ||
                                        toBinding.className().equals(Byte.class.getName()) ||
                                        toBinding.className().equals(byte.class.getName())
                                    )
                                ) return;
                            }

                            // vendor-specific
                            if (
                                toBinding.className().equals("java.time.OffsetDateTime") &&
                                fromColumn.sqlTypeName().equals("timestamptz") // postgresql
                            ) return;

                            // avoid JDBC 1.0 spec bug

                            if (
                                (toBinding.className().equals(Byte.class.getName()) &&
                                 fromColumn.sqlType() == Types.TINYINT
                                ) ||
                                (toBinding.className().equals(Short.class.getName()) &&
                                 fromColumn.sqlType() == Types.SMALLINT
                                )
                            ) return;

                            if (
                                (
                                    toBinding.className().equals(Integer.class.getName()) ||
                                    toBinding.className().equals(int.class.getName())
                                ) && fromColumn.javaClassName().equals(Integer.class.getName())
                            ) {

                                if (fromColumn.sqlType() == Types.TINYINT)
                                    throw new ValidationException(
                                        "type mismatch for column " + (columnIndex + 1) +
                                        (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                        ". Expected " + toBinding.className() + " but has java.lang.Byte"
                                    );

                                if (fromColumn.sqlType() == Types.SMALLINT)
                                    throw new ValidationException(
                                        "type mismatch for column " + (columnIndex + 1) +
                                        (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                        ". Expected " + toBinding.className() + " but has java.lang.Short"
                                    );
                            }

                            if (
                                (
                                    bindingNullMode == GLangParser.NullMode.DEFAULT_NOT_NULL ||
                                    bindingNullMode == GLangParser.NullMode.EXACTLY_NOT_NULL
                                ) && (
                                    (toBinding.className().equals(boolean.class.getName()) && (
                                        fromColumn.javaClassName().equals(Boolean.class.getName()) ||
                                        fromColumn.sqlType() == Types.BOOLEAN
                                    )) ||
                                    (toBinding.className().equals(byte.class.getName()) && (
                                        fromColumn.javaClassName().equals(Byte.class.getName()) ||
                                        fromColumn.sqlType() == Types.TINYINT
                                    )) ||
                                    (toBinding.className().equals(char.class.getName()) && (
                                        fromColumn.javaClassName().equals(Character.class.getName()) ||
                                        fromColumn.sqlType() == Types.CHAR
                                    )) ||
                                    (toBinding.className().equals(short.class.getName()) && (
                                        fromColumn.javaClassName().equals(Short.class.getName()) ||
                                        fromColumn.sqlType() == Types.SMALLINT
                                    )) ||
                                    (toBinding.className().equals(int.class.getName()) && (
                                        fromColumn.javaClassName().equals(Integer.class.getName()) ||
                                        fromColumn.sqlType() == Types.INTEGER
                                    )) ||
                                    (toBinding.className().equals(long.class.getName()) && (
                                        fromColumn.javaClassName().equals(Long.class.getName()) ||
                                        fromColumn.sqlType() == Types.BIGINT
                                    )) ||
                                    (toBinding.className().equals(float.class.getName()) && (
                                        fromColumn.javaClassName().equals(Float.class.getName()) ||
                                        fromColumn.sqlType() == Types.FLOAT
                                    )) ||
                                    (toBinding.className().equals(double.class.getName()) && (
                                        fromColumn.javaClassName().equals(Double.class.getName()) ||
                                        fromColumn.sqlType() == Types.DOUBLE
                                    ))
                                )
                            ) return;

                            if (
                                toBinding.compatibleSqlType() == null &&
                                toBinding.compatibleSqlTypeName() == null
                            ) {
                                if (!toBinding.className().equals(fromColumn.javaClassName()))
                                    throw new ValidationException(
                                        "type mismatch for column " + (columnIndex + 1) +
                                        (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                        ". Expected " + toBinding.className() + " but has " + fromColumn.javaClassName()
                                    );
                            } else {
                                if (toBinding.compatibleSqlTypeName() != null) {
                                    if (!toBinding.compatibleSqlTypeName().equals(fromColumn.sqlTypeName()))
                                        throw new ValidationException(
                                            "Sql type name mismatch for column " + (columnIndex + 1) +
                                            (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                            ". Expected " + toBinding.compatibleSqlTypeName() + " but has " + fromColumn.sqlTypeName()
                                        );
                                }

                                if (toBinding.compatibleSqlType() != null) {
                                    if (toBinding.compatibleSqlType() != fromColumn.sqlType())
                                        throw new ValidationException(
                                            "Sql type id (java.sql.Types) mismatch for column " + (columnIndex + 1) +
                                            (fieldName != null ? " (for field `" + fieldName + "`)" : "") +
                                            ". Expected " + toBinding.compatibleSqlType() + " but has " + fromColumn.sqlType()
                                        );
                                }
                            }
                        };

                        final GLangParser.FieldType toDto;

                        if (parsedConfig.url() != null) {
                            var query = forPrepare.queryMode().parts().stream()
                                .map(p -> switch (p) {
                                    case InoutParameter _,
                                         OutParameter _,
                                         SimpleParameter _ -> "?";
                                    case TextPart tp -> tp.text;
                                    case UnfoldParameter u -> {
                                        var n = unfoldArgumentsCount(u.extractor);
                                        yield IntStream.range(0, n)
                                            .mapToObj(_ -> "?")
                                            .collect(Collectors.joining(
                                                ", ", "(", ")"
                                            ));
                                    }
                                })
                                .collect(Collectors.joining());

                            try (
                                var connection = DriverManager.getConnection(
                                    parsedConfig.url(), parsedConfig.username(), parsedConfig.password()
                                );
                                var stmt = switch (statementMode) {
                                    case StatementGenerator.AsDefault _ ->
                                        connection.prepareStatement(query);
                                    case StatementGenerator.AsCall _ ->
                                        connection.prepareCall(query);
                                    case AsGeneratedKeysIndices gk -> connection.prepareStatement(
                                        query, gk.columnIndexes()
                                    );
                                    case AsGeneratedKeysColumnNames gk ->
                                        connection.prepareStatement(
                                            query, gk.columnNames()
                                        );
                                }
                            ) {

                                var onDatabase = connection.getMetaData().getDatabaseProductName();
                                final List<GLangParser.ColumnMetadata> columns;

                                switch (statementMode) {
                                    case StatementGenerator.AsDefault _,
                                         AsGeneratedKeysColumnNames _,
                                         AsGeneratedKeysIndices _ -> {

                                        var makeColumns = (Function<ResultSetMetaData, List<GLangParser.ColumnMetadata>>) rMetadata -> {
                                            if (rMetadata == null) return List.of();

                                            var isUppercaseOnly = (Function<String, Boolean>) str ->
                                                str.chars().filter(ch -> ch != '_')
                                                    .allMatch(Character::isUpperCase);

                                            var isGLangExpression = (Function<String, Boolean>) str ->
                                                str.contains(".") || str.contains(":t");

                                            var getColumnName = (BiFunction<ResultSetMetaData, Integer, String>) (mt, i) -> {
                                                try {
                                                    return switch (onDatabase) {
                                                        case POSTGRESQL_DB_NAME ->
                                                            (String) mt.getClass().getMethod("getBaseColumnName", int.class)
                                                                .invoke(mt, i);
                                                        case HSQL_DB_NAME -> {
                                                            var name = mt.getColumnName(i);
                                                            yield isUppercaseOnly.apply(name)
                                                                ? name.toLowerCase() : name;
                                                        }
                                                        case ORACLE_DB_NAME, MSSQL_DB_NAME -> null;
                                                        default -> mt.getColumnName(i);
                                                    };

                                                } catch (SQLException | NoSuchMethodException |
                                                         IllegalAccessException |
                                                         InvocationTargetException ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            };

                                            var fixedColumnLabel = (Function<String, String>) label -> {
                                                if (onDatabase.equals(MYSQL_DB_NAME) && label == null)
                                                    return ""; // ???

                                                if (onDatabase.equals(HSQL_DB_NAME) || onDatabase.equals(ORACLE_DB_NAME)) {
                                                    if (isGLangExpression.apply(label))
                                                        return label;
                                                    return isUppercaseOnly.apply(label)
                                                        ? label.toLowerCase() : label;
                                                }

                                                if (
                                                    onDatabase.equals(MSSQL_DB_NAME) &&
                                                    label.startsWith(":tnull")
                                                ) return ":t?" + label.substring(6);

                                                return label;
                                            };

                                            try {
                                                return IntStream.range(1, rMetadata.getColumnCount() + 1).mapToObj(i -> {
                                                    try {
                                                        return new GLangParser.ColumnMetadata(
                                                            switch (rMetadata.isNullable(i)) {
                                                                case
                                                                    ResultSetMetaData.columnNoNulls ->
                                                                    GLangParser.NullMode.EXACTLY_NOT_NULL;
                                                                case
                                                                    ResultSetMetaData.columnNullable ->
                                                                    GLangParser.NullMode.EXACTLY_NULLABLE;
                                                                case
                                                                    ResultSetMetaData.columnNullableUnknown ->
                                                                    GLangParser.NullMode.DEFAULT_NOT_NULL;
                                                                default ->
                                                                    throw new IllegalStateException("unreachable");
                                                            },
                                                            rMetadata.getColumnType(i),
                                                            rMetadata.getColumnTypeName(i),
                                                            rMetadata.getColumnClassName(i),
                                                            getColumnName.apply(rMetadata, i),
                                                            fixedColumnLabel.apply(rMetadata.getColumnLabel(i)),
                                                            rMetadata.getScale(i),
                                                            rMetadata.getPrecision(i)
                                                        );
                                                    } catch (SQLException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }).toList();
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        };


                                        if (
                                            onDatabase.equals(ORACLE_DB_NAME)
                                            && (
                                                statementMode instanceof AsGeneratedKeysIndices ||
                                                statementMode instanceof AsGeneratedKeysColumnNames
                                            )
                                        ) {
                                            stmt.getMetaData(); // runs query check
                                            columns = null;
                                        } else if (
                                            (
                                                onDatabase.equals(MYSQL_DB_NAME) ||
                                                onDatabase.equals(MARIA_DB_NAME)
                                            ) && (
                                                statementMode instanceof AsGeneratedKeysIndices ||
                                                statementMode instanceof AsGeneratedKeysColumnNames
                                            )
                                        )
                                            columns = makeColumns.apply(stmt.getGeneratedKeys().getMetaData());
                                        else
                                            columns = makeColumns.apply(stmt.getMetaData());
                                    }

                                    case StatementGenerator.AsCall _ -> {
                                        var pMetadata = stmt.getParameterMetaData();
                                        var parameters = forPrepare.queryMode.parts().stream()
                                            .filter(p ->
                                                !(p instanceof TextPart)
                                            ).toList();

                                        if (parameters.size() != pMetadata.getParameterCount())
                                            throw new ValidationException("parameter count mismatch");

                                        var outParameterIndexes = IntStream.range(0, parameters.size())
                                            .peek(i -> {
                                                try {
                                                    if (onDatabase.equals(ORACLE_DB_NAME))
                                                        return; // skip parameters check for oracle

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
                                                                throw new ValidationException(
                                                                    "For parameter " + (i + 1) +
                                                                    " mode mismatch. Expected IN but has " + pModeText
                                                                );
                                                        }
                                                        case InoutParameter _ -> {
                                                            if (
                                                                pMode != ParameterMetaData.parameterModeInOut &&
                                                                pMode != ParameterMetaData.parameterModeUnknown
                                                            )
                                                                throw new ValidationException(
                                                                    "For parameter " + (i + 1) +
                                                                    " mode mismatch. Expected INOUT but has " + pModeText
                                                                );
                                                        }
                                                        case OutParameter _ -> {
                                                            if (
                                                                pMode != ParameterMetaData.parameterModeOut &&
                                                                pMode != ParameterMetaData.parameterModeUnknown
                                                            )
                                                                throw new ValidationException(
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

                                        if (onDatabase.equals(ORACLE_DB_NAME)) columns = null;
                                        else
                                            columns = Arrays.stream(outParameterIndexes).mapToObj(i -> {
                                                try {
                                                    return new GLangParser.ColumnMetadata(
                                                        switch (pMetadata.isNullable(i)) {
                                                            case
                                                                ParameterMetaData.parameterNoNulls ->
                                                                GLangParser.NullMode.EXACTLY_NOT_NULL;
                                                            case
                                                                ParameterMetaData.parameterNullable ->
                                                                GLangParser.NullMode.EXACTLY_NULLABLE;
                                                            case
                                                                ParameterMetaData.parameterNullableUnknown ->
                                                                GLangParser.NullMode.DEFAULT_NOT_NULL;
                                                            default ->
                                                                throw new IllegalStateException("unreachable");
                                                        },
                                                        pMetadata.getParameterType(i),
                                                        pMetadata.getParameterTypeName(i),
                                                        pMetadata.getParameterClassName(i),
                                                        null, "a" + i,
                                                        pMetadata.getScale(i),
                                                        pMetadata.getPrecision(i)
                                                    );
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }).toList();

                                        statementMode = new StatementGenerator.AsCall(outParameterIndexes);
                                    }
                                }

                                toDto = switch (dtoMode) {
                                    case null -> null;
                                    case ExistingDto existing -> {
                                        var parsed = parseExistingDto.apply(existing);
                                        var dtoFields = flattedDtoType(parsed);

                                        // skip check if metadata is not available
                                        if (columns == null) yield parsed;

                                        if (dtoFields.size() != columns.size())
                                            throw new ValidationException(
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
                                                onDatabase, i, field.name(), binding, field.st().nullMode(), column
                                            );
                                        }

                                        yield parsed;
                                    }
                                    case GenerateDto gDto -> {
                                        if (columns == null) throw new ValidationException(
                                            ".g mode it not available because column metadata is not provided by JDBC driver"
                                        );

                                        var fields = parseResultSetColumns(
                                            columns, (column, columnIndex, javaClassNameHint, dtoNullMode) -> {

                                                final GLangParser.NullMode nullMode;
                                                if (dtoNullMode != GLangParser.NullMode.DEFAULT_NOT_NULL) {
                                                    checkNullability.check(
                                                        column.columnLabel(), dtoNullMode, columnIndex, column
                                                    );
                                                    nullMode = dtoNullMode;
                                                } else
                                                    nullMode = column.nullMode();

                                                final Standard.Binding binding;
                                                if (javaClassNameHint != null) {
                                                    binding = getBindingForClass.apply(
                                                        javaClassNameHint, nullMode
                                                    );
                                                    checkTypeCompatibility.check(
                                                        onDatabase, columnIndex, column.columnLabel(), binding, nullMode, column
                                                    );
                                                } else {
                                                    var inferType = (Supplier<String>) () -> {
                                                        // нужно как-то понять что нет конфликта по биндам
                                                        //
                                                        if (
                                                            nullMode == GLangParser.NullMode.DEFAULT_NOT_NULL ||
                                                            nullMode == GLangParser.NullMode.EXACTLY_NOT_NULL
                                                        ) {
                                                            if (
                                                                column.javaClassName().equals(Boolean.class.getName()) ||
                                                                column.sqlType() == Types.BOOLEAN
                                                            )
                                                                return boolean.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Byte.class.getName()) ||
                                                                column.sqlType() == Types.TINYINT
                                                            )
                                                                return byte.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Character.class.getName()) ||
                                                                column.sqlType() == Types.CHAR
                                                            )
                                                                return char.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Short.class.getName()) ||
                                                                column.sqlType() == Types.SMALLINT
                                                            )
                                                                return short.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Integer.class.getName()) ||
                                                                column.sqlType() == Types.INTEGER
                                                            )
                                                                return int.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Long.class.getName()) ||
                                                                column.sqlType() == Types.BIGINT
                                                            )
                                                                return long.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Float.class.getName()) ||
                                                                column.sqlType() == Types.FLOAT
                                                            )
                                                                return float.class.getName();

                                                            if (
                                                                column.javaClassName().equals(Double.class.getName()) ||
                                                                column.sqlType() == Types.DOUBLE
                                                            )
                                                                return double.class.getName();
                                                        }

                                                        // vendor-specific

                                                        if (onDatabase.equals(MSSQL_DB_NAME)) {
                                                            if (column.javaClassName().equals("microsoft.sql.DateTimeOffset"))
                                                                return "java.time.OffsetDateTime";
                                                        }

                                                        if (onDatabase.equals(ORACLE_DB_NAME)) {
                                                            if (column.javaClassName().equals("oracle.sql.TIMESTAMPTZ"))
                                                                return "java.time.ZonedDateTime";
                                                        }

                                                        // shim for new java.time API (JSR-310)
                                                        return switch (column.sqlType()) {
                                                            case Types.DATE ->
                                                                "java.time.LocalDate";
                                                            case Types.TIME ->
                                                                "java.time.LocalTime";
                                                            case Types.TIMESTAMP ->
                                                                // https://github.com/pgjdbc/pgjdbc/pull/3006
                                                                column.sqlTypeName().equals("timestamptz") // postgresql
                                                                    ? "java.time.OffsetDateTime"
                                                                    : "java.time.LocalDateTime";
                                                            case Types.TIME_WITH_TIMEZONE ->
                                                                "java.time.OffsetTime";
                                                            case Types.TIMESTAMP_WITH_TIMEZONE ->
                                                                column.javaClassName().equals("java.time.ZonedDateTime")
                                                                    ? column.javaClassName() : "java.time.OffsetDateTime";
                                                            default ->
                                                                parsedConfig.typeBindings().stream()
                                                                    .filter(b ->
                                                                        column.sqlTypeName().equals(b.compatibleSqlTypeName())
                                                                    )
                                                                    .findFirst()
                                                                    .map(Standard.Binding::className)
                                                                    .orElse(column.javaClassName());
                                                        };
                                                    };

                                                    binding = getBindingForClass.apply(
                                                        inferType.get(), nullMode
                                                    );
                                                }

                                                return new GLangParser.BindColumn.Result(binding.className(), nullMode);
                                            }
                                        );

                                        yield new GLangParser.AggregatedType(gDto.dtoClassName.toString(), fields);
                                    }
                                };

                            } catch (SQLException e) {
                                // FIXME: split this with `getConnection`
                                // isolate preparedStatement
                                throw new ValidationException(e.getMessage());
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

                                    if (statementMode instanceof StatementGenerator.AsCall) {
                                        var parameters = forPrepare.queryMode.parts().stream()
                                            .filter(p ->
                                                !(p instanceof TextPart)
                                            ).toList();

                                        var outParameterIndexes = IntStream.range(0, parameters.size())
                                            .filter(i -> !(parameters.get(i) instanceof SimpleParameter))
                                            .map(i -> i + 1)
                                            .toArray();

                                        if (outParameterIndexes.length != dtoFields.size())
                                            throw new ValidationException(
                                                "expected " + dtoFields.size() + " columns for dto but has " +
                                                outParameterIndexes.length + " INOUT/OUT parameters"
                                            );
                                    }

                                    yield parsed;
                                }
                                case GenerateDto _ -> throw new ValidationException(
                                    "compile time checks must be enabled for .g"
                                );
                            };
                        }

                        var fetchMode = switch (fetchMethodName) {
                            case "fetchOne" -> new StatementGenerator.FetchOne(toDto);
                            case "fetchOneOrZero" -> new StatementGenerator.FetchOneOrZero(toDto);
                            case "fetchList" -> new StatementGenerator.FetchList(toDto);
                            case "fetchStream" -> new StatementGenerator.FetchStream(toDto);
                            case "fetchNone" -> new StatementGenerator.FetchNone();
                            default -> throw new RuntimeException("unreachable");
                        };

                        var generatedCode = generate(
                            (className) ->
                                parsedConfig.typeBindings().stream()
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

                        if (context.get(HashMap.class) == null)
                            context.put(HashMap.class, new HashMap());

                        ((HashMap<JCTree.JCCompilationUnit, HashMap<JCTree.JCMethodInvocation, Object>>) context.get(HashMap.class))
                            .computeIfAbsent(cu, _ -> new HashMap<>())
                            .put(forPrepare.tree, doofyEncode(new TrueSqlPlugin.Invocation(
                                parsedConfig.typeBindings(), generatedClassName, fetchMethodName, lineNumber,
                                sourceExpression, forPrepare.queryMode, null
                            )));

                        result.add(generatedCode);
                    }
                }
            }

            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                try {
                    handleInvocation(tree);
                } catch (ValidationException e) {
                    messages.write(cu, tree, JCDiagnostic.DiagnosticType.ERROR, e.getMessage());
                }
                super.visitApply(tree);
            }
        }.scan(tree);

        return result;
    }
}
