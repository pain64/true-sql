package com.truej.sql.compiler;

import com.truej.sql.v3.bindings.UrlReadWrite;
import com.truej.sql.v3.config.TypeReadWrite;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class GenerateStandartBindings {

    record Binding(
        String typeName, String typeValueName, boolean mayBeNull,
        String accessorSuffix, String sqlType
    ) { }

    <T> void write(Binding binding) throws IOException {
        var toFile = System.getProperty("user.dir") + "/src/main/java/com/truej/sql/v3/bindings/" +
                     binding.typeName + "ReadWrite.java";

        var code = STR."""
            package com.truej.sql.v3.bindings;

            import com.truej.sql.v3.config.TypeReadWrite;

            import java.sql.*;

            public class \{binding.typeName}ReadWrite implements TypeReadWrite<\{binding.typeValueName}> {

                @Override public \{binding.typeValueName} get(
                    ResultSet rs, int columnIndex
                ) throws SQLException {
                    var value = rs.get\{binding.accessorSuffix}(columnIndex);
                    \{binding.mayBeNull ? "" : "if (rs.wasNull())"}
                    \{binding.mayBeNull ? "" : "    throw new IllegalStateException(\"null not expected\");"}
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
                        throw new IllegalStateException("null not expected");

                    return v;
                }

                @Override public void registerOutParameter(
                    CallableStatement stmt, int parameterIndex
                ) throws SQLException {
                    stmt.registerOutParameter(parameterIndex, Types.\{binding.sqlType});
                }
            }
            """;

        Files.write(
            Paths.get(toFile),
            code.getBytes()
        );
    }

    // may be null?, sqlType
    interface EE<B> extends TypeReadWrite<B> { }

    class ReadWriteAsObject<A> implements EE<A> {
        boolean mayBeNull() { return true; }
        int sqlType() { return Types.OTHER; }

        @Override public A get(
            ResultSet rs, int columnIndex
        ) throws SQLException {
            return (A) rs.getObject(columnIndex);
        }

        @Override public void set(
            PreparedStatement stmt, int parameterIndex, A value
        ) throws SQLException {
            stmt.setObject(parameterIndex, value);
        }

        @Override public A get(
            CallableStatement stmt, int parameterIndex
        ) throws SQLException {
            return (A) stmt.getObject(parameterIndex);
        }

        @Override public void registerOutParameter(
            CallableStatement stmt, int parameterIndex
        ) throws SQLException {
            stmt.registerOutParameter(parameterIndex, sqlType());
        }
    }

    class PgPointReadWrite extends ReadWriteAsObject<PGpoint> { }
    class PgPointReadWriteX extends PgPointReadWrite { }
    class Int2DimArrayReadWrite extends ReadWriteAsObject<int[][]> { }

    Class<?> up(HashMap<Type, Type> context, Type from) {
        Function<Type, Class<?>> doo = t -> {
            if (t instanceof Class<?> cl2) {
                return up(context, cl2);
            } else if (t instanceof ParameterizedType pt) {
                var next = (Class<?>) pt.getRawType();
                for (var i = 0; i < next.getTypeParameters().length; i++) {
                    var old = context.get(pt.getActualTypeArguments()[i]);
                    context.put(
                        next.getTypeParameters()[i],
                        old != null ? old : pt.getActualTypeArguments()[i]
                    );
                }

                return up(context, next);
            }

            return null;
        };

        if (from instanceof Class<?> cl) {
            if (cl == TypeReadWrite.class)
                return (Class<?>) context.get(cl.getTypeParameters()[0]);
            else {
                for (var iface : cl.getGenericInterfaces()) {
                    var r = doo.apply(iface);
                    if (r != null) return r;
                }

                return doo.apply(cl.getGenericSuperclass());
            }
        }

        return null;
    }

    Class<?> extractBoundType(Class<?> from) {
        return up(new HashMap<>(), from);
    }

    @Test void bar() throws IOException {
        var c = Int2DimArrayReadWrite.class;
        System.out.println("result = " + extractBoundType(PgPointReadWriteX.class));
        System.out.println("result = " + extractBoundType(Int2DimArrayReadWrite.class));

        // ((ParameterizedType)
        //        PgPointReadWrite.class.getGenericSuperclass())
        //        .getActualTypeArguments()[0];



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

        for (var b : bindings) write(b);
    }
}
