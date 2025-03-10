package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.GLangParser.NullMode;
import net.truej.sql.compiler.StatementGenerator.AsDefault;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysColumnNames;
import net.truej.sql.compiler.StatementGenerator.AsGeneratedKeysIndices;
import net.truej.sql.compiler.StatementGenerator.StatementMode;
import net.truej.sql.fetch.Parameters;
import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.truej.sql.compiler.ExistingDtoParser.flattenedDtoType;
import static net.truej.sql.compiler.TrueSqlPlugin.*;


public class InvocationsFinder {
    public sealed interface QueryPart { }
    public record TextPart(String text) implements QueryPart { }
    public sealed interface ParameterPart extends QueryPart { }
    sealed interface SingleParameter extends ParameterPart { }
    sealed interface InOrInoutParameter extends SingleParameter {
        JCTree.JCExpression expression();
    }

    public record InParameter(JCTree.JCExpression expression) implements InOrInoutParameter { }
    public record InoutParameter(JCTree.JCExpression expression) implements InOrInoutParameter { }
    public record OutParameter(Type toType) implements SingleParameter { }
    public record UnfoldParameter(
        JCTree.JCExpression expression, @Nullable JCTree.JCLambda extractor
    ) implements ParameterPart { }

    record ExistingDto(
        NullMode nullMode, Type toType
    ) implements DtoMode { }

    record GenerateDto(Name dtoClassName) implements DtoMode { }

    sealed interface DtoMode { }

    private static class DeferredValidationException extends RuntimeException {
        final JCTree tree;
        DeferredValidationException(JCTree tree, String message) {
            super(message);
            this.tree = tree;
        }
    }

    static StatementGenerator.Query parseQuery(
        Symtab symtab, JCTree.JCCompilationUnit cu, Names names,
        JCTree.JCMethodInvocation tree,
        // FIXME: two nullable parameters with corelation
        JCTree.JCExpression batchListDataExpression, JCTree.JCLambda batchLambda,
        String queryText, com.sun.tools.javac.util.List<JCTree.JCExpression> args
    ) {
        var clParameters = symtab.enterClass(
            cu.modle, names.fromString(Parameters.class.getName())
        );

        interface NotInParameterParser {
            QueryPart parse(Name name, JCTree.JCMethodInvocation invoke);
        }

        var parser = (NotInParameterParser) (methodName, invoke) -> {
            final QueryPart parameter;

            if (methodName.equals(names.fromString("inout")))
                parameter = new InoutParameter(invoke.args.head);
            else if (methodName.equals(names.fromString("out")))
                parameter = new OutParameter(
                    TypeFinder.resolve(names, symtab, cu, invoke.args.head)
                );
            else { // unfold
                if (invoke.args.size() == 1)
                    parameter = new UnfoldParameter(invoke.args.head, null);
                else { // 2
                    if (
                        invoke.args.get(1) instanceof JCTree.JCLambda lmb &&
                        lmb.body instanceof JCTree.JCNewArray array &&
                        array.elemtype instanceof JCTree.JCIdent elementTypeId &&
                        elementTypeId.name.equals(names.fromString("Object"))
                    )
                        parameter = new UnfoldParameter(invoke.args.head, lmb);
                    else
                        throw new ValidationException(
                            invoke,
                            "Unfold parameter extractor must be lambda literal returning " +
                            "object array literal (e.g. `u -> new Object[]{u.f1, u.f2}`)"
                        );
                }
            }

            if (batchLambda != null)
                throw new ValidationException(
                    invoke, "only IN parameters allowed in batch mode"
                );

            return parameter;
        };

        var fragments = (queryText + " ").split("(?<=[\\s,=()])\\?");
        var result = new ArrayList<QueryPart>();

        if (fragments.length - 1 != args.size())
            throw new ValidationException(
                tree, "parameter count mismatch. expected "
                      + (fragments.length - 1) + " but has " + args.size()
            );

        for (var i = 0; i < fragments.length; i++) {
            result.add(new TextPart(fragments[i]));

            if (i < args.size()) {
                var expression = args.get(i);
                var parsed = (QueryPart) new InParameter(expression);

                if (expression instanceof JCTree.JCMethodInvocation invoke) {
                    if (invoke.meth instanceof JCTree.JCIdent id) {
                        var imp = cu.starImportScope.findFirst(id.name);
                        if (imp == null)
                            imp = cu.namedImportScope.findFirst(id.name);

                        if (
                            imp != null &&
                            imp instanceof Symbol.MethodSymbol &&
                            imp.owner == clParameters
                        )
                            parsed = parser.parse(id.name, invoke);
                    } else if (
                        invoke.meth instanceof JCTree.JCFieldAccess fa && (
                            (
                                fa.selected instanceof JCTree.JCIdent id &&
                                id.name.equals(clParameters.getSimpleName()) &&
                                cu.namedImportScope.findFirst(id.name) instanceof Symbol s &&
                                s.type.tsym == clParameters
                            ) || (
                                fa.selected instanceof JCTree.JCFieldAccess faa &&
                                clParameters.getQualifiedName().contentEquals(faa.toString())
                            )
                        )
                    )
                        parsed = parser.parse(fa.name, invoke);
                }

                result.add(parsed);
            }
        }

        return batchLambda != null
            ? new StatementGenerator.BatchedQuery(batchListDataExpression, batchLambda, result)
            : new StatementGenerator.SingleQuery(result);
    }

    static ValidationException badSource(JCTree tree) {
        return new ValidationException(
            tree,
            "Source identifier may be method parameter, class field or " +
            "local variable initialized by new (var ds = new MySourceW(...)). " +
            "Type of source identifier cannot be generic parameter"
        );
    }

    static LinkedHashMap<JCTree.JCMethodInvocation, MethodInvocationResult> find(
        Symtab symtab, Names names, CompilerMessages messages,
        JCTree.JCCompilationUnit cu, JCTree tree, String generatedClassName
    ) {

        var clConnectionW = symtab.enterClass(
            cu.modle, names.fromString(ConnectionW.class.getName())
        );
        var clDataSourceW = symtab.enterClass(
            cu.modle, names.fromString(DataSourceW.class.getName())
        );

        var result = new LinkedHashMap<JCTree.JCMethodInvocation, MethodInvocationResult>();

        new TreeScanner() {
            final Map<Name, Symbol.ClassSymbol> varTypes = new HashMap<>();
            final Set<Name> withConnectionSource = new HashSet<>();

            @Override public void visitVarDef(JCTree.JCVariableDecl tree) {
                if (tree.sym == null) {
                    var exprTree = tree.vartype != null ? tree.vartype :
                        tree.init instanceof JCTree.JCNewClass cl ? cl.clazz : null;

                    var found = exprTree == null ? null : TypeFinder.find(symtab, cu, exprTree);

                    if (found != null && found.tsym instanceof Symbol.ClassSymbol cl)
                        varTypes.put(tree.name, cl);
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

            void handleConstraint(
                JCTree.JCMethodInvocation tree, JCTree.JCExpression sourceExpression
            ) throws SQLException {
                if (
                    tree.args.length() >= 3 && tree.args.length() <= 5
                ) {

                    if (
                        tree.args.get(tree.args.length() - 3) instanceof JCTree.JCLiteral l3 &&
                        l3.getValue() instanceof String tableName &&
                        tree.args.get(tree.args.length() - 2) instanceof JCTree.JCLiteral l4 &&
                        l4.getValue() instanceof String constraintName
                    ) {
                        var schemaName = (String) null;
                        var catalogName = (String) null;

                        if (tree.args.length() >= 4) {
                            if (
                                tree.args.get(tree.args.length() - 4) instanceof JCTree.JCLiteral l2 &&
                                l2.getValue() instanceof String sn
                            )
                                schemaName = sn;
                            else
                                return;
                        }

                        if (tree.args.length() == 5) {
                            if (
                                tree.args.getFirst() instanceof JCTree.JCLiteral l1 &&
                                l1.getValue() instanceof String cn
                            )
                                catalogName = cn;
                            else
                                return;
                        }

                        if (!(sourceExpression instanceof JCTree.JCIdent))
                            throw new DeferredValidationException(
                                sourceExpression, "Expected identifier for source for `.constraint(...)`"
                            );

                        var vt = varTypes.get(((JCTree.JCIdent) sourceExpression).name);
                        if (
                            vt == null ||
                            vt.getSuperclass().tsym != clConnectionW &&
                            vt.getSuperclass().tsym != clDataSourceW
                        ) throw new DeferredValidationException(
                            tree, badSource(tree).getMessage()
                        );

                        var parsedConfig = ConfigurationParser.parseConfig(
                            symtab, names, cu, vt, (__, ___) -> { }
                        );
                        if (parsedConfig.url() != null)
                            JdbcMetadataFetcher.checkConstraint(
                                tree, parsedConfig, catalogName, schemaName, tableName, constraintName
                            );
                    }
                }
            }

            @Nullable FetchInvocation handleFetch(
                JCTree.JCMethodInvocation tree, JCTree.JCFieldAccess f
            ) throws SQLException {

                var lastError = (ValidationException) null;
                var warnings = new ArrayList<CompilationWarning>();
                var fetchMethodName = f.name.toString();

                if (
                    !f.name.equals(names.fromString("fetchOne")) &&
                    !f.name.equals(names.fromString("fetchOneOrZero")) &&
                    !f.name.equals(names.fromString("fetchList")) &&
                    !f.name.equals(names.fromString("fetchStream")) &&
                    !f.name.equals(names.fromString("fetchNone"))
                )
                    return null;

                var lineNumber = cu.getLineMap().getLineNumber(tree.getStartPosition());
                var dtoMode = (DtoMode) null;

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
                        lastError = new ValidationException(tree.args.head, "Expected %NewDtoClassName%.class");

                    f = fa;
                } else {
                    if (f.name.equals(names.fromString("fetchNone"))) {
                        if (!tree.args.isEmpty()) return null;
                        dtoMode = null;
                    } else if (tree.args.size() == 1) {
                        dtoMode = new ExistingDto(
                            NullMode.DEFAULT_NOT_NULL,
                            TypeFinder.resolve(names, symtab, cu, tree.args.head)
                        );
                    } else if (tree.args.size() == 2) {
                        final NullMode nullMode;
                        if (
                            (tree.args.head instanceof JCTree.JCIdent id &&
                             id.name.equals(names.fromString("Nullable"))) ||
                            tree.args.head.toString().equals(Parameters.class.getSimpleName() + ".Nullable") ||
                            tree.args.head.toString().equals(Parameters.class.getName() + ".Nullable")
                        ) {
                            nullMode = NullMode.EXACTLY_NULLABLE;
                        } else if (
                            (tree.args.head instanceof JCTree.JCIdent id &&
                             id.name.equals(names.fromString("NotNull"))) ||
                            tree.args.head.toString().equals(Parameters.class.getSimpleName() + ".NotNull") ||
                            tree.args.head.toString().equals(Parameters.class.getName() + ".NotNull")
                        ) {
                            nullMode = NullMode.EXACTLY_NOT_NULL;
                        } else
                            return null;

                        dtoMode = new ExistingDto(
                            nullMode, TypeFinder.resolve(names, symtab, cu, tree.args.get(1))
                        );
                    } else
                        return null;
                }

                boolean isWithUpdateCount = false;
                if (
                    f.selected instanceof JCTree.JCFieldAccess fa &&
                    fa.name.equals(names.fromString("withUpdateCount"))
                ) {
                    isWithUpdateCount = true;
                    f = fa;
                }

                var statementMode = (StatementMode) new AsDefault();

                if (
                    f.selected instanceof JCTree.JCMethodInvocation inv &&
                    inv.meth instanceof JCTree.JCFieldAccess fa
                ) {
                    if (
                        fa.name.equals(names.fromString("asGeneratedKeys")) && !inv.args.isEmpty()
                    ) {
                        if (inv.args.stream().allMatch(
                            arg -> arg instanceof JCTree.JCLiteral al &&
                                   al.getValue() instanceof Integer
                        )) {
                            statementMode = new AsGeneratedKeysIndices(

                                inv.args.stream().mapToInt(arg ->
                                    (Integer) ((JCTree.JCLiteral) arg).value
                                ).toArray()
                            );
                            f = fa;
                        } else if (inv.args.stream().allMatch(arg ->
                            arg instanceof JCTree.JCLiteral al && (
                                al.getValue() instanceof String ||
                                al.getValue() == null
                            )
                        )) {
                            if (
                                inv.args.stream()
                                    .anyMatch(arg -> ((JCTree.JCLiteral) arg).getValue() == null)
                            )
                                lastError = new ValidationException(
                                    tree, "unexpected null literal as asGeneratedKeys column name"
                                );

                            statementMode = new AsGeneratedKeysColumnNames(
                                inv.args.stream().map(arg ->
                                    (String) ((JCTree.JCLiteral) arg).value
                                ).toArray(String[]::new)
                            );
                            f = fa;
                        }
                    } else if (
                        fa.name.equals(names.fromString("asCall")) && inv.args.isEmpty()
                    ) {
                        statementMode = new StatementGenerator.AsCall();
                        f = fa;
                    }
                }

                var query = (StatementGenerator.Query) null;
                var sourceExpression = (JCTree.JCIdent) null;
                var sourceMode = (StatementGenerator.SourceMode) null;
                var parsedConfig = (ConfigurationParser.ParsedConfiguration) null;

                if (
                    f.selected instanceof JCTree.JCMethodInvocation inv &&
                    inv.meth instanceof JCTree.JCFieldAccess fa &&
                    fa.name.equals(names.fromString("q"))
                ) {
                    var tryAsQuery = (Function<Integer, @Nullable String>) i ->
                        i < inv.args.length() &&
                        inv.args.get(i) instanceof JCTree.JCLiteral lit &&
                        lit.getValue() instanceof String text ? text : null;


                    var queryText = tryAsQuery.apply(0);
                    if (queryText != null) // single
                        try {
                            query = parseQuery(
                                symtab, cu, names, tree, null, null, queryText, inv.args.tail
                            );
                        } catch (ValidationException e) {
                            lastError = e;
                        }
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
                                try {
                                    query = parseQuery(
                                        symtab, cu, names, tree, inv.args.head, lmb, queryText, array.elems
                                    );
                                } catch (ValidationException e) {
                                    lastError = e;
                                }
                            else lastError = new ValidationException(
                                tree,
                                "Batch parameter extractor must be lambda literal returning " +
                                "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
                            );
                        } else
                            lastError = new ValidationException(
                                tree, "Query text must be a string literal"
                            );
                    }

                    if (fa.selected instanceof JCTree.JCIdent processorId) {
                        sourceExpression = processorId;

                        var vt = varTypes.get(processorId.name);

                        if (vt != null && vt.getSuperclass().tsym == clConnectionW) {
                            sourceMode = StatementGenerator.SourceMode.CONNECTION;
                        } else if (vt != null && vt.getSuperclass().tsym == clDataSourceW) {
                            if (withConnectionSource.contains(processorId.name))
                                sourceMode = StatementGenerator.SourceMode.CONNECTION;
                            else
                                sourceMode = StatementGenerator.SourceMode.DATASOURCE;
                        } else
                            throw badSource(processorId);

                        parsedConfig = ConfigurationParser.parseConfig(
                            symtab, names, cu, vt, (__, ___) -> { }
                        );

                    } else throw new ValidationException(
                        fa.selected, "Expected identifier for source for `.q(...)`"
                    );
                } else
                    return null;

                if (lastError != null) throw lastError;

                // mitigate Java language limitations
                var _query = query;
                var _parsedConfig = parsedConfig;

                var parseExistingDto = (Function<ExistingDto, GLangParser.FetchToField>) existing ->
                    (GLangParser.FetchToField) ExistingDtoParser.parse(
                        _parsedConfig.typeBindings(), true, false, "result",
                        existing.nullMode, names.init, existing.toType
                    );

                final GLangParser.FetchToField toDto;
                var jdbcMetadata = parsedConfig.url() == null ? null :
                    JdbcMetadataFetcher.fetch(
                        parsedConfig.url(), parsedConfig.username(), parsedConfig.password(),
                        query, statementMode
                    );

                var stMode = statementMode;

                var makeColumns = (Supplier<@Nullable List<GLangParser.ColumnMetadata>>) () -> {
                    var mapColumns = (Supplier<@Nullable List<GLangParser.ColumnMetadata>>) () ->
                        jdbcMetadata.columns() == null ? null :
                            jdbcMetadata.columns().stream().map(c ->
                                new GLangParser.ColumnMetadata(
                                    c.nullMode(),
                                    c.sqlType(),
                                    c.sqlTypeName(),
                                    c.javaClassName(),
                                    c.columnName(),
                                    c.columnLabel(),
                                    c.scale(),
                                    c.precision()
                                )
                            ).toList();

                    return switch (stMode) {
                        case StatementGenerator.StatementLike __ -> mapColumns.get();
                        case StatementGenerator.AsCall __ -> {
                            if (jdbcMetadata.onDatabase().equals(DatabaseNames.POSTGRESQL_DB_NAME))
                                yield mapColumns.get();

                            // FIXME: проверка call и unfold одновременно? ???
                            // FIXME: проверка batch и unfold одновременно? ???
                            var pMetadata = jdbcMetadata.parameters();
                            if (pMetadata == null) yield null;

                            yield Arrays.stream(getOutParametersNumbers(_query))
                                .mapToObj(i -> {
                                    var p = pMetadata.get(i - 1);
                                    return new GLangParser.ColumnMetadata(
                                        p.nullMode(),
                                        p.sqlType(),
                                        p.sqlTypeName(),
                                        p.javaClassName(),
                                        null,
                                        "c" + i,
                                        p.scale(),
                                        p.precision()
                                    );
                                }).toList();
                        }
                    };
                };

                var handleNullabilityMismatch = (BiConsumer<Boolean, String>)
                    (isWarningOnly, message) -> {
                        if (isWarningOnly)
                            warnings.add(new CompilationWarning(tree, message));
                        else
                            throw new ValidationException(tree, message);
                    };

                toDto = switch (dtoMode) {
                    case null -> null;
                    case ExistingDto existing -> {
                        var parsed = parseExistingDto.apply(existing);
                        var dtoFields = flattenedDtoType(parsed);

                        if (jdbcMetadata == null) yield parsed;
                        var columns = makeColumns.get();
                        if (columns == null) yield parsed;

                        if (dtoFields.size() != columns.size())
                            throw new ValidationException(
                                tree, "target type implies " + dtoFields.size() + " columns but result has "
                                      + columns.size()
                            );

                        for (var i = 0; i < dtoFields.size(); i++) {
                            var ii = i;
                            var field = dtoFields.get(i);
                            var column = columns.get(i);

                            TypeChecker.assertNullabilityCompatible(
                                column.nullMode(),
                                field.nullMode(),
                                (isWarningOnly, sqlNullMode, javaNullMode) ->
                                    handleNullabilityMismatch.accept(
                                        isWarningOnly,
                                        "nullability mismatch for column " + (ii + 1) +
                                        " (for field `" + field.name() + "`). Your decision is " +
                                        javaNullMode + " but driver infers " + sqlNullMode
                                    )
                            );

                            TypeChecker.assertTypesCompatible(
                                jdbcMetadata.onDatabase(),
                                column.sqlType(), // FIXME: pass column ???
                                column.sqlTypeName(),
                                column.javaClassName(),
                                column.scale(),
                                field.binding(),
                                (typeKind, expected, has) -> new ValidationException(
                                    tree, typeKind + " mismatch for column " + (ii + 1) +
                                          " (for field `" + field.name() + "`). Expected " +
                                          expected + " but has " + has
                                )
                            );
                        }

                        yield parsed;
                    }
                    case GenerateDto gDto -> {
                        if (jdbcMetadata == null)
                            throw new ValidationException(
                                tree, "compile time checks must be enabled for .g"
                            );

                        var columns = makeColumns.get();

                        if (columns == null)
                            throw new ValidationException(
                                tree, ".g mode is not available because column metadata is not provided by JDBC driver"
                            );

                        var fields = GLangParser.parseResultSetColumns(
                            columns, (column, columnIndex, javaClassNameHint, nullModeHint) -> {

                                final NullMode nullMode;
                                if (nullModeHint != NullMode.DEFAULT_NOT_NULL) {
                                    TypeChecker.assertNullabilityCompatible(
                                        column.nullMode(), nullModeHint,
                                        (isWarningOnly, sqlNullMode, javaNullMode) ->
                                            handleNullabilityMismatch.accept(
                                                // FIXME: deduplicate message common part
                                                isWarningOnly,
                                                "nullability mismatch for column " + (columnIndex + 1) +
                                                " (for generated dto field `" + column.columnLabel() + "`). Your decision is " +
                                                javaNullMode + " but driver infers " + sqlNullMode
                                            )
                                    );
                                    nullMode = nullModeHint;
                                } else
                                    nullMode = column.nullMode();

                                final Standard.Binding binding;
                                if (javaClassNameHint != null) {
                                    binding = TypeChecker.getBindingForClass(
                                        tree, _parsedConfig.typeBindings(), false, javaClassNameHint
                                    );

                                    TypeChecker.assertTypesCompatible(
                                        jdbcMetadata.onDatabase(), column.sqlType(), column.sqlTypeName(),
                                        column.javaClassName(), column.scale(), binding,
                                        (typeKind, expected, has) -> new ValidationException(
                                            tree, typeKind + " mismatch for column " + (columnIndex + 1) +
                                                  " (for generated dto field `" + column.columnLabel() +
                                                  "`). Expected " + expected + " but has " + has
                                        )
                                    );
                                } else
                                    binding = TypeChecker.getBindingForClass(
                                        tree, _parsedConfig.typeBindings(), true, TypeChecker.inferType(
                                            _parsedConfig, jdbcMetadata.onDatabase(), column, nullMode
                                        )
                                    );

                                return new GLangParser.BindColumn.Result(
                                    binding, nullMode
                                );
                            }
                        );

                        yield new GLangParser.ListOfGroupField(
                            "result", gDto.dtoClassName.toString(), fields
                        );
                    }
                };

                var fetchMode = switch (fetchMethodName) {
                    case "fetchOne" -> new StatementGenerator.FetchOne(toDto);
                    case "fetchOneOrZero" -> new StatementGenerator.FetchOneOrZero(toDto);
                    case "fetchList" -> new StatementGenerator.FetchList(toDto);
                    case "fetchStream" -> new StatementGenerator.FetchStream(toDto);
                    default /* fetchNone */ -> new StatementGenerator.FetchNone();
                };

                var generatedCode = StatementGenerator.generate(
                    className ->
                        _parsedConfig.typeBindings().stream()
                            .filter(b -> b.className().equals(className))
                            .findFirst().get().rwClassName().replace('$', '.'),
                    lineNumber,
                    sourceMode,
                    query,
                    statementMode,
                    fetchMode,
                    dtoMode instanceof GenerateDto,
                    isWithUpdateCount
                );

                return new FetchInvocation(
                    jdbcMetadata == null ? null : jdbcMetadata.onDatabase(),
                    parsedConfig.typeBindings(),
                    generatedClassName, fetchMethodName, lineNumber, warnings,
                    sourceExpression, query,
                    jdbcMetadata == null ? null : jdbcMetadata.parameters(),
                    generatedCode
                );
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
                } catch (
                    ValidationException |
                    GLangParser.ParseException |
                    ExistingDtoParser.ParseException |
                    ConfigurationParser.ParseException |
                    SQLException e
                ) {
                    // Эти ошибки нужно отдать в javac именно в этот момент.
                    // Если закончится раунд процессинга аннотаций, а ошибка не записана
                    // то у нас не сгенерируются Dto и будет ошибка компиляции
                    // и до плагина компилятора мы не дойдем!
                    messages.write(cu, tree, JCDiagnostic.DiagnosticType.ERROR, e.getMessage());
                } catch (
                    DeferredValidationException e
                ) {
                    result.put(tree, new ValidationError(tree, e.getMessage()));
                }

                super.visitApply(tree);
            }
        }.scan(tree);

        return result;
    }

    static int[] getOutParametersNumbers(StatementGenerator.Query query) {
        var parameters = query.parts().stream()
            .filter(p -> !(p instanceof TextPart)).toList();

        return IntStream.range(
            0, parameters.size()
        ).filter(i ->
            parameters.get(i) instanceof InoutParameter ||
            parameters.get(i) instanceof OutParameter
        ).map(i -> i + 1).toArray();
    }
}
