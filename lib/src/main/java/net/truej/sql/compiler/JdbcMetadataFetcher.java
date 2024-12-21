package net.truej.sql.compiler;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.sql.ParameterMetaData.*;
import static net.truej.sql.compiler.DatabaseNames.*;
import static net.truej.sql.compiler.DatabaseNames.MARIA_DB_NAME;
import static net.truej.sql.compiler.StatementGenerator.unfoldArgumentsCount;

public class JdbcMetadataFetcher {

    public record SqlColumnMetadata(
        String javaClassName, String sqlTypeName, int sqlType,
        int scale, int precision,
        @Nullable String columnName, String columnLabel,
        GLangParser.NullMode nullMode
    ) { }

    public record SqlParameterMetadata(
        String javaClassName, String sqlTypeName, int sqlType,
        int scale, int precision,
        GLangParser.NullMode nullMode,   // FIXME: use `int`
        TrueSqlPlugin.ParameterMode mode // FIXME: use `int`
    ) { }

    record JdbcMetadata(
        String onDatabase,
        @Nullable List<SqlColumnMetadata> columns,
        @Nullable List<SqlParameterMetadata> parameters
    ) { }

    private static List<SqlColumnMetadata> fetchColumns(
        String onDatabase, ResultSetMetaData rMetadata
    ) throws SQLException {
        // FIXME: remove this line
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

        return new ArrayList<>() {{
            for (var i = 1; i < rMetadata.getColumnCount() + 1; i++)
                add(
                    new SqlColumnMetadata(
                        rMetadata.getColumnClassName(i),
                        rMetadata.getColumnTypeName(i),
                        rMetadata.getColumnType(i),
                        rMetadata.getScale(i),
                        rMetadata.getPrecision(i),
                        getColumnName.apply(rMetadata, i),
                        fixedColumnLabel.apply(rMetadata.getColumnLabel(i)),
                        switch (rMetadata.isNullable(i)) {
                            case ResultSetMetaData.columnNoNulls ->
                                GLangParser.NullMode.EXACTLY_NOT_NULL;
                            case ResultSetMetaData.columnNullable ->
                                GLangParser.NullMode.EXACTLY_NULLABLE;
                            default -> // columnNullableUnknown
                                GLangParser.NullMode.DEFAULT_NOT_NULL;
                        }
                    )
                );
        }};
    }

    private static List<SqlParameterMetadata> fetchParameters(
        ParameterMetaData pmt
    ) throws SQLException {
        return new ArrayList<>() {{
            for (var i = 1; i < pmt.getParameterCount() + 1; i++)
                add(
                    new SqlParameterMetadata(
                        pmt.getParameterClassName(i),
                        pmt.getParameterTypeName(i),
                        pmt.getParameterType(i),
                        pmt.getScale(i),
                        pmt.getPrecision(i),
                        switch (pmt.isNullable(i)) {
                            case ParameterMetaData.parameterNoNulls ->
                                GLangParser.NullMode.EXACTLY_NOT_NULL;
                            case ParameterMetaData.parameterNullable ->
                                GLangParser.NullMode.EXACTLY_NULLABLE;
                            default ->  // parameterNullableUnknown
                                GLangParser.NullMode.DEFAULT_NOT_NULL;
                        },
                        switch (pmt.getParameterMode(i)) {
                            case parameterModeIn -> TrueSqlPlugin.ParameterMode.IN;
                            case parameterModeInOut -> TrueSqlPlugin.ParameterMode.INOUT;
                            case parameterModeOut -> TrueSqlPlugin.ParameterMode.OUT;
                            default -> // parameterModeUnknown
                                throw new RuntimeException("unreachable");
                        }
                    )
                );
        }};
    }

    private static boolean isDriversLoaded = false;


    static JdbcMetadata fetch(
        String url, String username, String password,
        StatementGenerator.QueryMode queryMode,
        StatementGenerator.StatementMode statementMode
    ) throws SQLException {

        var query = queryMode.parts().stream()
            .map(p -> switch (p) {
                case InvocationsFinder.SingleParameter __ -> "?";
                case InvocationsFinder.TextPart tp -> tp.text();
                case InvocationsFinder.UnfoldParameter u -> {
                    var n = unfoldArgumentsCount(u.extractor());
                    yield IntStream.range(0, n)
                        .mapToObj(__ -> "?")
                        .collect(Collectors.joining(
                            ", ", "(", ")"
                        ));
                }
            })
            .collect(Collectors.joining());

        if (!isDriversLoaded) {
            isDriversLoaded = true;
            for (var driver : ServiceLoader.load(
                Driver.class, TrueSqlAnnotationProcessor.class.getClassLoader()
            ))
                DriverManager.registerDriver(driver);
        }

        try (
            var connection = DriverManager.getConnection(url, username, password);
            var stmt = switch (statementMode) {
                case StatementGenerator.AsDefault __ -> connection.prepareStatement(query);
                case StatementGenerator.AsCall __ -> connection.prepareCall(query);
                case StatementGenerator.AsGeneratedKeysIndices gk -> connection.prepareStatement(
                    query, gk.columnIndexes()
                );
                case StatementGenerator.AsGeneratedKeysColumnNames gk ->
                    connection.prepareStatement(
                        query, gk.columnNames()
                    );
            }
        ) {

            var onDatabase = connection.getMetaData().getDatabaseProductName();
            final List<SqlColumnMetadata> columns;

            if (
                onDatabase.equals(MYSQL_DB_NAME) &&
                statementMode instanceof StatementGenerator.AsCall
            ) {
                columns = null;
            } else if (
                onDatabase.equals(ORACLE_DB_NAME)
                && (
                    statementMode instanceof StatementGenerator.AsGeneratedKeysIndices ||
                    statementMode instanceof StatementGenerator.AsGeneratedKeysColumnNames
                )
            ) {
                stmt.getMetaData(); // runs query check
                columns = null;
            } else if (
                (
                    onDatabase.equals(MYSQL_DB_NAME) ||
                    onDatabase.equals(MARIA_DB_NAME)
                ) && (
                    statementMode instanceof StatementGenerator.AsGeneratedKeysIndices ||
                    statementMode instanceof StatementGenerator.AsGeneratedKeysColumnNames
                )
            )
                columns = fetchColumns(onDatabase, stmt.getGeneratedKeys().getMetaData());
            else
                columns = fetchColumns(onDatabase, stmt.getMetaData());


            final List<SqlParameterMetadata> parameters;

            if (
                onDatabase.equals(MYSQL_DB_NAME)
            )
                parameters = null;
            else {
                List<SqlParameterMetadata> parsed;
                try {
                    parsed = fetchParameters(stmt.getParameterMetaData());
                } catch (SQLFeatureNotSupportedException e) {
                    parsed = null;
                }

                parameters = parsed;
            }

            return new JdbcMetadata(onDatabase, columns, parameters);
        }
    }
}
