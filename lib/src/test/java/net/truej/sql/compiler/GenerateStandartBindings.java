package net.truej.sql.compiler;

import net.truej.sql.bindings.AsObjectReadWrite;
import net.truej.sql.config.TypeReadWrite;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateStandartBindings {

    record Binding(
        String typeName, String typeValueName, boolean mayBeNull,
        String accessorSuffix, String sqlType
    ) { }

    <T> void write(Binding binding) throws IOException {
        var toFile = System.getProperty("user.dir") + "/src/main/java/net/truej/sql/bindings/" +
                     binding.typeName + "ReadWrite.java";

        var code = STR."""
            package net.truej.sql.bindings;

            import net.truej.sql.config.TypeReadWrite;

            import java.sql.*;

            public class \{binding.typeName}ReadWrite implements TypeReadWrite<\{binding.typeValueName}> {

                @Override public \{binding.typeValueName} get(
                    ResultSet rs, int columnIndex
                ) throws SQLException {
                    var value = rs.get\{binding.accessorSuffix}(columnIndex);
                    if (rs.wasNull())
                        \{binding.mayBeNull ? "return null;" : "throw new IllegalStateException(\"null not expected\");"}
                    return value;
                }

                @Override public void set(
                    PreparedStatement stmt, int parameterIndex, \{binding.typeValueName} value
                ) throws SQLException {
                    \{binding.mayBeNull ? "" : "if (value == null)"}
                    \{binding.mayBeNull ? "" : "    throw new IllegalStateException(\"null not expected\");"}
                    stmt.set\{binding.accessorSuffix}(parameterIndex, value);
                }

                @Override public \{binding.typeValueName} get(
                    CallableStatement stmt, int parameterIndex
                ) throws SQLException {
                    var v = stmt.get\{binding.accessorSuffix}(parameterIndex);
                    if (stmt.wasNull())
                        \{binding.mayBeNull ? "return null;" : "throw new IllegalStateException(\"null not expected\");"}

                    return v;
                }

                @Override public void registerOutParameter(
                    CallableStatement stmt, int parameterIndex
                ) throws SQLException {
                    stmt.registerOutParameter(parameterIndex, Types.\{binding.sqlType});
                }
            }
            """;

        Files.write(Paths.get(toFile), code.getBytes());
    }

    @Test void bar() throws IOException {

        var bindings = List.of(
            new Binding("PrimitiveBoolean", "java.lang.Boolean", false, "Boolean", "BOOLEAN"),
            new Binding("Boolean", "java.lang.Boolean", true, "Boolean", "BOOLEAN"),

            new Binding("PrimitiveByte", "java.lang.Byte", false, "Byte", "TINYINT"),
            new Binding("Byte", "java.lang.Byte", true, "Byte", "TINYINT"),

            new Binding("PrimitiveShort", "java.lang.Short", false, "Short", "SMALLINT"),
            new Binding("Short", "java.lang.Short", true, "Short", "SMALLINT"),

            new Binding("PrimitiveInt", "java.lang.Integer", false, "Int", "INTEGER"),
            new Binding("Integer", "java.lang.Integer", true, "Int", "INTEGER"),

            new Binding("PrimitiveLong", "java.lang.Long", false, "Long", "BIGINT"),
            new Binding("Long", "java.lang.Long", true, "Long", "BIGINT"),

            new Binding("String", "java.lang.String", true, "String", "VARCHAR"),
            new Binding("Date", "java.sql.Date", true, "Date", "VARCHAR"),
            new Binding("Time", "java.sql.Time", true, "Time", "VARCHAR"),
            new Binding("Timestamp", "java.sql.Timestamp", true, "Timestamp", "VARCHAR"),
            new Binding("BigDecimal", "java.math.BigDecimal", true, "BigDecimal", "NUMERIC"),
            new Binding("ByteArray", "byte[]", true, "Bytes", "VARBINARY"),
            new Binding("Url", "java.net.URL", true, "URL", "VARCHAR")
        );

        for (var b : bindings)
            if (!b.typeName.equals("Timestamp"))
                write(b);

        var instances = bindings.stream().map(b ->
            "new Binding(" +
            "\"" + (b.typeName.startsWith("Primitive") ? b.typeName.replace("Primitive", "").toLowerCase(): b.typeValueName) + "\", "+
            "\"net.truej.sql.bindings." + b.typeName + "ReadWrite\", " +
            b.mayBeNull + ", " +
            "null, null)"
        ).collect(Collectors.joining(",\n        "));

        Files.write(
            Paths.get(
                System.getProperty("user.dir") + "/src/main/java/net/truej/sql/bindings/Standard.java"
            ),
            STR."""
                package net.truej.sql.bindings;

                import org.jetbrains.annotations.Nullable;

                import java.util.List;

                public class Standard {
                    public record Binding(
                        String className, String rwClassName, boolean mayBeNullable,
                        @Nullable Integer compatibleSqlType, @Nullable String compatibleSqlTypeName
                    ) {}

                    public static final List<Binding> bindings = List.of(
                        \{instances}
                    );
                }
                """.getBytes()
        );
    }
}
