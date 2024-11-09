package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysColumnNames;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysIndices;
import net.truej.sql.config.Configuration;
import net.truej.sql.fetch.Parameters;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.sql.ParameterMetaData.*;
import static net.truej.sql.compiler.ExistingDtoParser.flattedDtoType;
import static net.truej.sql.compiler.ExistingDtoParser.parse;
import static net.truej.sql.compiler.GLangParser.parseResultSetColumns;
import static net.truej.sql.compiler.StatementGenerator.unfoldArgumentsCount;
import static net.truej.sql.compiler.TrueSqlPlugin.*;
import static net.truej.sql.compiler.DatabaseNames.*;


public class InvocationsFinder {
    public sealed interface QueryPart { }
    public record TextPart(String text) implements QueryPart { }
    sealed interface InOrInoutParameter extends QueryPart {
        JCTree.JCExpression expression();
    }

    public record InParameter(JCTree.JCExpression expression) implements InOrInoutParameter { }
    public record InoutParameter(JCTree.JCExpression expression) implements InOrInoutParameter { }
    public record OutParameter(Symbol.ClassSymbol toClass) implements QueryPart { }
    public record UnfoldParameter(
        JCTree.JCExpression expression, @Nullable JCTree.JCLambda extractor
    ) implements QueryPart { }

    record ExistingDto(
        GLangParser.NullMode nullMode, Symbol.ClassSymbol symbol
    ) implements DtoMode { }

    record GenerateDto(Name dtoClassName) implements DtoMode { }

    sealed interface DtoMode { }

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
                        invoke, sName + " parameter is not applicable in batch mode"
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
                            invoke,
                            "Unfold parameter extractor must be lambda literal returning " +
                            "object array literal (e.g. `u -> new Object[]{u.f1, u.f2}`)"
                        );
                } else
                    throw new ValidationException(invoke, "bad tree"); // FIXME: refactor to new bad tree subsystem
            } else
                return new InParameter(invoke);
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
                            parsed = new InParameter(invoke);
                    } else
                        parsed = new InParameter(invoke);
                } else
                    parsed = new InParameter(expression);

                result.add(parsed);
            }
        }

        return batchLambda != null
            ? new StatementGenerator.BatchedQuery(batchListDataExpression, batchLambda, result)
            : new StatementGenerator.SingleQuery(result);
    }

    static LinkedHashMap<JCTree.JCMethodInvocation, MethodInvocationResult> find(
        ProcessingEnvironment env, Context context, Symtab symtab, Names names,
        TreeMaker maker, CompilerMessages messages, JCTree.JCCompilationUnit cu, JCTree tree,
        String generatedClassName
    ) {

        var result = new LinkedHashMap<JCTree.JCMethodInvocation, MethodInvocationResult>();

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

            void handleConstraint(JCTree.JCMethodInvocation tree, JCTree.JCExpression sourceExpression) {
                // FIXME: move to if-condition
                if (tree.args.length() < 3 || tree.args.length() > 5) return;

                // TODO: validate
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
                                        tree, "constraint not found"
                                    );

                                var full = new CatalogAndSchema(
                                    rs.getString(1), rs.getString(2)
                                );

                                if (rs.next()) throw new ValidationException(
                                    tree, "too much constraints for criteria"
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

                        if (
                            onDatabase.equals(MYSQL_DB_NAME) ||
                            onDatabase.equals(MARIA_DB_NAME)
                        )
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
                            case 3: // tableName + constraintName
                                if (
                                    tree.args.get(0) instanceof JCTree.JCLiteral l1 &&
                                    l1.getValue() instanceof String tableName &&
                                    tree.args.get(1) instanceof JCTree.JCLiteral l2 &&
                                    l2.getValue() instanceof String constraintName
                                ) {
                                    var realCatalog = connection.getCatalog();
                                    var realSchema = connection.getSchema();

                                    // FIXME: correct resolve for PostgreSQL (search_path)

                                    if (
                                        onDatabase.equals(MYSQL_DB_NAME) ||
                                        onDatabase.equals(MARIA_DB_NAME)
                                    ) {
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
                            case 4: // schemaName + tableName + constraintName
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
                            default:  // 5 - catalogName + schemaName + tableName + constraintName
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
            }

            @Nullable FetchInvocation handleFetch(JCTree.JCMethodInvocation tree, JCTree.JCFieldAccess f) {
                // FIXME: split to parse tree and handle
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
                        if (tree.args.isEmpty()) return null;

                        if (
                            tree.args.head instanceof JCTree.JCFieldAccess gfa &&
                            gfa.name.equals(names.fromString("class")) &&
                            gfa.selected instanceof JCTree.JCIdent gid
                        )
                            dtoMode = new GenerateDto(gid.name);
                        else
                            return null; // TODO: check that .class literal

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
                                // FIXME: use FQN
                                tree.args.head.toString().equals("net.truej.sql.fetch.Parameters.Nullable")
                            ) {
                                nullMode = GLangParser.NullMode.EXACTLY_NULLABLE;
                            } else if (
                                (tree.args.head instanceof JCTree.JCIdent id &&
                                 id.name.equals(names.fromString("NotNull"))) ||
                                tree.args.head.toString().equals("net.truej.sql.fetch.Parameters.NotNull")
                            ) {
                                nullMode = GLangParser.NullMode.EXACTLY_NOT_NULL;
                            } else
                                return null;

                            dtoMode = f.name.equals(names.fromString("fetchNone")) ? null :
                                new ExistingDto(
                                    nullMode, TypeFinder.resolve(names, symtab, cu, tree.args.get(1))
                                );
                        } else
                            return null;
                    }

                    boolean isWithUpdateCount = false;
                    if (f.selected instanceof JCTree.JCFieldAccess fa) {
                        if (fa.name.equals(names.fromString("withUpdateCount")))
                            isWithUpdateCount = true;
                        else
                            return null;
                        f = fa;
                    }

                    var statementMode = (StatementGenerator.StatementMode)
                        new StatementGenerator.AsDefault();

                    if (f.selected instanceof JCTree.JCMethodInvocation inv) {
                        if (inv.meth instanceof JCTree.JCFieldAccess fa) {

                            if (fa.name.equals(names.fromString("asGeneratedKeys"))) {
                                if (inv.args.isEmpty())
                                    return null;
                                if (inv.args.getFirst() instanceof JCTree.JCLiteral lit) {
                                    if (lit.getValue() instanceof Integer) {

                                        if (!inv.args.stream().allMatch(
                                            arg -> arg instanceof JCTree.JCLiteral al &&
                                                   al.getValue() instanceof Integer
                                        )) return null;

                                        statementMode = new AsGeneratedKeysIndices(

                                            inv.args.stream().mapToInt(arg ->
                                                (Integer) ((JCTree.JCLiteral) arg).value
                                            ).toArray()
                                        );
                                    } else if (lit.getValue() instanceof String) {
                                        if (!inv.args.stream().allMatch(arg ->
                                            arg instanceof JCTree.JCLiteral al &&
                                            al.getValue() instanceof String
                                        )) return null;

                                        statementMode = new AsGeneratedKeysColumnNames(
                                            inv.args.stream().map(arg ->
                                                (String) ((JCTree.JCLiteral) arg).value
                                            ).toArray(String[]::new)
                                        );
                                    } else
                                        return null;
                                } else
                                    return null;

                                f = fa;
                            } else if (fa.name.equals(names.fromString("asCall"))) {
                                statementMode = new StatementGenerator.AsCall(new int[]{});
                                f = fa;
                            }

                        } else
                            return null;
                    }

                    final StatementGenerator.QueryMode queryMode;
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


                                var queryText = tryAsQuery.apply(0);
                                if (queryText != null) // single
                                    queryMode = parseQuery(
                                        symtab, cu, names, null, null, queryText, inv.args.tail
                                    );
                                else {
                                    queryText = tryAsQuery.apply(1);
                                    if (queryText != null) { // batch
                                        if (inv.args.size() != 3)
                                            return null;

                                        if (
                                            inv.args.get(2) instanceof JCTree.JCLambda lmb &&
                                            lmb.body instanceof JCTree.JCNewArray array &&
                                            array.elemtype instanceof JCTree.JCIdent elementTypeId &&
                                            elementTypeId.name.equals(names.fromString("Object"))
                                        )
                                            queryMode = parseQuery(
                                                symtab, cu, names, inv.args.head, lmb, queryText, array.elems
                                            );
                                        else throw new ValidationException(
                                            tree,
                                            "Batch parameter extractor must be lambda literal returning " +
                                            "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
                                        );
                                    } else
                                        throw new ValidationException(
                                            tree, "Query text must be a string literal"
                                        );
                                }

                                if (fa.selected instanceof JCTree.JCIdent processorId) {
                                    sourceExpression = processorId;
                                    var clConnectionW = symtab.enterClass(
                                        cu.modle, names.fromString("net.truej.sql.source.ConnectionW")
                                    );
                                    var clDataSourceW = symtab.enterClass(
                                        cu.modle, names.fromString("net.truej.sql.source.DataSourceW")
                                    );


                                    var vt = varTypes.get(processorId.name);

                                    if (vt != null && vt.getSuperclass().tsym == clConnectionW) {
                                        sourceMode = StatementGenerator.SourceMode.CONNECTION;
                                    } else if (vt != null && vt.getSuperclass().tsym == clDataSourceW) {
                                        if (withConnectionSource.contains(processorId.name))
                                            sourceMode = StatementGenerator.SourceMode.CONNECTION;
                                        else
                                            sourceMode = StatementGenerator.SourceMode.DATASOURCE;
                                    } else
                                        throw new ValidationException(
                                            processorId,
                                            "Source identifier may be method parameter, class field or " +
                                            "local variable initialized by new (var ds = new MySourceW(...)). " +
                                            "Type of source identifier cannot be generic parameter"
                                        );


                                    sourceType = vt;
                                    parsedConfig = ConfigurationParser.parseConfig(
                                        env, symtab, names, cu,
                                        sourceType.className(),
                                        vt.getAnnotation(Configuration.class),
                                        false
                                    );

                                } else throw new ValidationException(
                                    fa.selected, "Expected identifier for source for `.q(...)`"
                                );
                            } else return null;
                        } else return null;
                    } else return null;

                    var parseExistingDto = (Function<ExistingDto, GLangParser.FieldType>) existing ->
                        parse(
                            existing.nullMode,
                            cl -> parsedConfig.typeBindings().stream()
                                .anyMatch(b -> b.className().equals(cl)),
                            names.init, existing.symbol
                        );

                    final GLangParser.FieldType toDto;
                    final List<ParameterMetadata> parametersMetadata;
                    final String onDatabase;

                    if (parsedConfig.url() != null) {
                        var query = queryMode.parts().stream()
                            .map(p -> switch (p) {
                                case InoutParameter _,
                                     OutParameter _,
                                     InParameter _ -> "?";
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
                                case StatementGenerator.AsCall _ -> connection.prepareCall(query);
                                case AsGeneratedKeysIndices gk -> connection.prepareStatement(
                                    query, gk.columnIndexes()
                                );
                                case AsGeneratedKeysColumnNames gk -> connection.prepareStatement(
                                    query, gk.columnNames()
                                );
                            }
                        ) {

                            onDatabase = connection.getMetaData().getDatabaseProductName();

                            if (
                                onDatabase.equals(MYSQL_DB_NAME)
                            )
                                parametersMetadata = null;
                            else {
                                var parsed = new ArrayList<ParameterMetadata>();
                                try {
                                    var pmt = stmt.getParameterMetaData();

                                    for (var i = 1; i < pmt.getParameterCount() + 1; i++)
                                        parsed.add(
                                            new ParameterMetadata(
                                                pmt.getParameterClassName(i),
                                                pmt.getParameterTypeName(i),
                                                pmt.getParameterType(i),
                                                pmt.getScale(i),
                                                switch (pmt.getParameterMode(i)) {
                                                    case parameterModeUnknown ->
                                                        ParameterMode.UNKNOWN;
                                                    case parameterModeIn -> ParameterMode.IN;
                                                    case parameterModeInOut -> ParameterMode.INOUT;
                                                    case parameterModeOut -> ParameterMode.OUT;
                                                    default ->
                                                        throw new RuntimeException("unreachable");
                                                }
                                            )
                                        );
                                } catch (SQLFeatureNotSupportedException e) {
                                    parsed = null;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                                parametersMetadata = parsed;
                            }

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
                                                            case ResultSetMetaData.columnNoNulls ->
                                                                GLangParser.NullMode.EXACTLY_NOT_NULL;
                                                            case ResultSetMetaData.columnNullable ->
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
                                    var parameters = queryMode.parts().stream()
                                        .filter(p ->
                                            !(p instanceof TextPart)
                                        ).toList();

                                    if (parameters.size() != pMetadata.getParameterCount())
                                        throw new ValidationException(tree, "parameter count mismatch");

                                    var outParameterIndexes = IntStream.range(0, parameters.size())
                                        .peek(i -> {
                                            try {
                                                if (onDatabase.equals(ORACLE_DB_NAME))
                                                    return; // skip parameters check for oracle

                                                var pMode = pMetadata.getParameterMode(i + 1);
                                                var pModeText = switch (pMode) {
                                                    case parameterModeIn -> "IN";
                                                    case parameterModeInOut -> "INOUT";
                                                    case parameterModeOut -> "OUT";
                                                    case parameterModeUnknown -> "UNKNOWN";
                                                    default ->
                                                        throw new IllegalStateException("unreachable");
                                                };

                                                switch (parameters.get(i)) {
                                                    case InParameter _ -> {
                                                        if (
                                                            pMode != parameterModeIn &&
                                                            pMode != parameterModeUnknown
                                                        )
                                                            throw new ValidationException(
                                                                tree, "For parameter " + (i + 1) +
                                                                      " mode mismatch. Expected IN but has " + pModeText
                                                            );
                                                    }
                                                    case InoutParameter _ -> {
                                                        if (
                                                            pMode != parameterModeInOut &&
                                                            pMode != parameterModeUnknown
                                                        )
                                                            throw new ValidationException(
                                                                tree, "For parameter " + (i + 1) +
                                                                      " mode mismatch. Expected INOUT but has " + pModeText
                                                            );
                                                    }
                                                    case OutParameter _ -> {
                                                        if (
                                                            pMode != parameterModeOut &&
                                                            pMode != parameterModeUnknown
                                                        )
                                                            throw new ValidationException(
                                                                tree, "For parameter " + (i + 1) +
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
                                        .filter(i -> !(parameters.get(i) instanceof InParameter))
                                        .map(i -> i + 1)
                                        .toArray();

                                    if (onDatabase.equals(ORACLE_DB_NAME)) columns = null;
                                    else
                                        columns = Arrays.stream(outParameterIndexes).mapToObj(i -> {
                                            try {
                                                return new GLangParser.ColumnMetadata(
                                                    switch (pMetadata.isNullable(i)) {
                                                        case parameterNoNulls ->
                                                            GLangParser.NullMode.EXACTLY_NOT_NULL;
                                                        case parameterNullable ->
                                                            GLangParser.NullMode.EXACTLY_NULLABLE;
                                                        case parameterNullableUnknown ->
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
                                            tree, "target type implies " + dtoFields.size() + " columns but result has "
                                                  + columns.size()
                                        );

                                    for (var i = 0; i < dtoFields.size(); i++) {
                                        var field = dtoFields.get(i);
                                        var column = columns.get(i);

                                        // FIXME: deduplicate
                                        var binding = TypeChecker.getBindingForClass(
                                            tree, parsedConfig.typeBindings(), field.st().javaClassName(), field.st().nullMode()
                                        );

                                        TypeChecker.assertNullabilityCompatible(
                                            cu, messages, tree,
                                            field.name(), field.st().nullMode(), i, column
                                        );

                                        TypeChecker.assertTypesCompatible(
                                            tree, onDatabase, i, column.sqlType(), column.sqlTypeName(),
                                            column.javaClassName(), column.scale(), field.name(), binding
                                        );
                                    }

                                    yield parsed;
                                }
                                case GenerateDto gDto -> {
                                    if (columns == null) throw new ValidationException(
                                        tree, ".g mode is not available because column metadata is not provided by JDBC driver"
                                    );

                                    var fields = parseResultSetColumns(
                                        columns, (column, columnIndex, javaClassNameHint, nullModeHint) -> {

                                            final GLangParser.NullMode nullMode;
                                            if (nullModeHint != GLangParser.NullMode.DEFAULT_NOT_NULL) {
                                                TypeChecker.assertNullabilityCompatible(
                                                    cu, messages, tree,
                                                    column.columnLabel(), nullModeHint, columnIndex, column
                                                );
                                                nullMode = nullModeHint;
                                            } else
                                                nullMode = column.nullMode();

                                            final Standard.Binding binding;
                                            if (javaClassNameHint != null) {
                                                binding = TypeChecker.getBindingForClass(
                                                    tree, parsedConfig.typeBindings(), javaClassNameHint, nullMode
                                                );
                                                TypeChecker.assertTypesCompatible(
                                                    tree, onDatabase, columnIndex, column.sqlType(), column.sqlTypeName(),
                                                    column.javaClassName(), column.scale(), column.columnLabel(), binding
                                                );
                                            } else
                                                binding = TypeChecker.getBindingForClass(
                                                    tree, parsedConfig.typeBindings(), TypeChecker.inferType(parsedConfig, onDatabase, column, nullMode), nullMode
                                                );

                                            return new GLangParser.BindColumn.Result(binding.className(), nullMode);
                                        }
                                    );

                                    yield new GLangParser.AggregatedType(gDto.dtoClassName.toString(), fields);
                                }
                            };

                        } catch (SQLException e) {
                            // FIXME: split this with `getConnection`
                            // isolate preparedStatement
                            throw new ValidationException(tree, e.getMessage());
                        }
                    } else { // no compile time checks
                        onDatabase = null;
                        parametersMetadata = null;
                        toDto = switch (dtoMode) {
                            case null -> null;
                            case ExistingDto existing -> {
                                var parsed = parseExistingDto.apply(existing);
                                var dtoFields = flattedDtoType(parsed);

                                for (var field : dtoFields) {
                                    var _ = TypeChecker.getBindingForClass(
                                        tree, parsedConfig.typeBindings(), field.st().javaClassName(), field.st().nullMode()
                                    );
                                }

                                if (statementMode instanceof StatementGenerator.AsCall) {
                                    var parameters = queryMode.parts().stream()
                                        .filter(p ->
                                            !(p instanceof TextPart)
                                        ).toList();

                                    var outParameterIndexes = IntStream.range(0, parameters.size())
                                        .filter(i -> !(parameters.get(i) instanceof InParameter))
                                        .map(i -> i + 1)
                                        .toArray();

                                    if (outParameterIndexes.length != dtoFields.size())
                                        throw new ValidationException(
                                            tree, "expected " + dtoFields.size() + " columns for dto but has " +
                                                  outParameterIndexes.length + " INOUT/OUT parameters"
                                        );
                                }

                                yield parsed;
                            }
                            case GenerateDto _ -> throw new ValidationException(
                                tree, "compile time checks must be enabled for .g"
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

                    var generatedCode = StatementGenerator.generate(
                        className ->
                            parsedConfig.typeBindings().stream()
                                .filter(b -> b.className().equals(className))
                                .findFirst().orElseThrow(() -> new IllegalStateException("unreachable"))
                                .rwClassName(),
                        lineNumber,
                        sourceMode,
                        queryMode,
                        statementMode,
                        fetchMode,
                        dtoMode instanceof InvocationsFinder.GenerateDto,
                        isWithUpdateCount
                    );

                    return new FetchInvocation(
                        onDatabase, parsedConfig.typeBindings(),
                        generatedClassName, fetchMethodName, lineNumber,
                        sourceExpression, queryMode, parametersMetadata, generatedCode
                    );
                }

                return null;
            }

            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                try {
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
                        // Fixme: check that receiver is JCIdent
                        // foo().withConnection() is not allowed!
                        var found = varTypes.get(id.name);
                        if (found != null) { // FIXME: unknown source error
                            var cnParameterName = lmb.params.head.name;
                            varTypes.put(cnParameterName, found);
                            withConnectionSource.add(cnParameterName);
                        }
                    } else if (
                        tree.meth instanceof JCTree.JCFieldAccess fa &&
                        fa.name.equals(names.fromString("constraint"))
                    ) {
                        handleConstraint(tree, fa.selected);
                    } else if (tree.meth instanceof JCTree.JCFieldAccess f) {
                        var fetchInvocation = handleFetch(tree, f);
                        if (fetchInvocation != null)
                            result.put(tree, fetchInvocation);
                    }
                } catch (ValidationException e) {
                    result.put(tree, e);
                } catch (GLangParser.ParseException | ExistingDtoParser.ParseException e) {
                    result.put(tree, new ValidationException(tree, e.getMessage()));
                }

                super.visitApply(tree);
            }
        }.scan(tree);

        return result;
    }
}
