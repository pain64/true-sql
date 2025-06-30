package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.truej.sql.compiler.ConfigurationParser.*;
import static net.truej.sql.compiler.ExistingDtoParser.flattenedDtoType;

public class InvocationsHandler {
    sealed interface Task {
        ParsedConfiguration config();
        JCTree.JCMethodInvocation tree();
    }
    record ConstraintCheckTask(
        ParsedConfiguration config,
        com.sun.tools.javac.tree.JCTree.JCMethodInvocation tree, String catalogName,
        String schemaName, String tableName, String constraintName
    ) implements Task { }

    record FetchInvocationTask(
        ParsedConfiguration config, JCTree.JCMethodInvocation tree, String fetchMethodName, String generatedClassName,
        int lineNumber, StatementGenerator.SourceMode sourceMode,
        com.sun.tools.javac.tree.JCTree.JCIdent sourceExpression, StatementGenerator.Query query,
        StatementGenerator.StatementMode statementMode, InvocationsFinder.DtoMode dtoMode,
        boolean isWithUpdateCount
    ) implements Task { }

    static void handleConstraint(
        @Nullable Connection connection, ConstraintCheckTask task
    ) throws SQLException {
        if (connection != null)
            JdbcMetadataFetcher.checkConstraint(
                connection, task.tree, task.catalogName,
                task.schemaName, task.tableName, task.constraintName
            );
    }

    static int[] getOutParametersNumbers(StatementGenerator.Query query) {
        var parameters = query.parts().stream()
            .filter(p -> !(p instanceof InvocationsFinder.TextPart)).toList();

        return IntStream.range(
            0, parameters.size()
        ).filter(i ->
            parameters.get(i) instanceof InvocationsFinder.InoutParameter ||
            parameters.get(i) instanceof InvocationsFinder.OutParameter
        ).map(i -> i + 1).toArray();
    }

    static TrueSqlPlugin.FetchInvocation handleInvocation(
        Names names, Connection connection, FetchInvocationTask task
    ) throws SQLException {
        var warnings = new ArrayList<TrueSqlPlugin.CompilationWarning>();

        var parseExistingDto = (Function<InvocationsFinder.ExistingDto, GLangParser.FetchToField>) existing ->
            (GLangParser.FetchToField) ExistingDtoParser.parse(
                task.config.typeBindings(), true, false, "result",
                existing.nullMode(), names.init, existing.toType()
            );

        final GLangParser.FetchToField toDto;
        var jdbcMetadata = connection == null ? null :
            JdbcMetadataFetcher.fetch(
                connection, task.query, task.statementMode
            );

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

            return switch (task.statementMode) {
                case StatementGenerator.StatementLike __ -> mapColumns.get();
                case StatementGenerator.AsCall __ -> {
                    if (jdbcMetadata.onDatabase().equals(DatabaseNames.POSTGRESQL_DB_NAME))
                        yield mapColumns.get();

                    // FIXME: проверка call и unfold одновременно? ???
                    // FIXME: проверка batch и unfold одновременно? ???
                    var pMetadata = jdbcMetadata.parameters();
                    if (pMetadata == null) yield null;

                    yield Arrays.stream(getOutParametersNumbers(task.query))
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
                    warnings.add(new TrueSqlPlugin.CompilationWarning(task.tree, message));
                else
                    throw new TrueSqlPlugin.ValidationException(task.tree, message);
            };

        toDto = switch (task.dtoMode) {
            case null -> null;
            case InvocationsFinder.ExistingDto existing -> {
                var parsed = parseExistingDto.apply(existing);
                var dtoFields = flattenedDtoType(parsed);

                if (jdbcMetadata == null) yield parsed;
                var columns = makeColumns.get();
                if (columns == null) yield parsed;

                if (dtoFields.size() != columns.size())
                    throw new TrueSqlPlugin.ValidationException(
                        task.tree, "target type implies " + dtoFields.size() + " columns but result has "
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
                        (typeKind, expected, has) -> new TrueSqlPlugin.ValidationException(
                            task.tree, typeKind + " mismatch for column " + (ii + 1) +
                                  " (for field `" + field.name() + "`). Expected " +
                                  expected + " but has " + has
                        )
                    );
                }

                yield parsed;
            }
            case InvocationsFinder.GenerateDto gDto -> {
                if (jdbcMetadata == null)
                    throw new TrueSqlPlugin.ValidationException(
                        task.tree, "compile time checks must be enabled for .g"
                    );

                var columns = makeColumns.get();

                if (columns == null)
                    throw new TrueSqlPlugin.ValidationException(
                        task.tree, ".g mode is not available because column metadata is not provided by JDBC driver"
                    );

                var fields = GLangParser.parseResultSetColumns(
                    columns, (column, columnIndex, javaClassNameHint, nullModeHint) -> {

                        final GLangParser.NullMode nullMode;
                        if (nullModeHint != GLangParser.NullMode.DEFAULT_NOT_NULL) {
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
                                task.tree, task.config.typeBindings(), false, javaClassNameHint
                            );

                            TypeChecker.assertTypesCompatible(
                                jdbcMetadata.onDatabase(), column.sqlType(), column.sqlTypeName(),
                                column.javaClassName(), column.scale(), binding,
                                (typeKind, expected, has) -> new TrueSqlPlugin.ValidationException(
                                    task.tree, typeKind + " mismatch for column " + (columnIndex + 1) +
                                          " (for generated dto field `" + column.columnLabel() +
                                          "`). Expected " + expected + " but has " + has
                                )
                            );
                        } else
                            binding = TypeChecker.getBindingForClass(
                                task.tree, task.config.typeBindings(), true, TypeChecker.inferType(
                                    task.config, jdbcMetadata.onDatabase(), column, nullMode
                                )
                            );

                        return new GLangParser.BindColumn.Result(
                            binding, nullMode
                        );
                    }
                );

                yield new GLangParser.ListOfGroupField(
                    "result", false, gDto.dtoClassName().toString(), fields
                );
            }
        };

        var fetchMode = switch (task.fetchMethodName) {
            case "fetchOne" -> new StatementGenerator.FetchOne(toDto);
            case "fetchOneOrZero" -> new StatementGenerator.FetchOneOrZero(toDto);
            case "fetchList" -> new StatementGenerator.FetchList(toDto);
            case "fetchStream" -> new StatementGenerator.FetchStream(toDto);
            default /* fetchNone */ -> new StatementGenerator.FetchNone();
        };

        var generatedCode = StatementGenerator.generate(
            className ->
                task.config.typeBindings().stream()
                    .filter(b -> b.className().equals(className))
                    .findFirst().get().rwClassName().replace('$', '.'),
            task.lineNumber,
            task.sourceMode,
            task.query,
            task.statementMode,
            fetchMode,
            task.dtoMode instanceof InvocationsFinder.GenerateDto,
            task.isWithUpdateCount
        );

        return new TrueSqlPlugin.FetchInvocation(
            jdbcMetadata == null ? null : jdbcMetadata.onDatabase(),
            task.config.typeBindings(),
            task.generatedClassName, task.fetchMethodName, task.lineNumber, warnings,
            task.sourceExpression, task.query,
            jdbcMetadata == null ? null : jdbcMetadata.parameters(),
            generatedCode
        );
    }
}
