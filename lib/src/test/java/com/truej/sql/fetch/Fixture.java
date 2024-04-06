package com.truej.sql.fetch;

import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.config.*;
import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.prepare.Statement;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Fixture {
    public static Statement queryStmt(String text) {
        return new Statement() {
            @Override protected RuntimeException mapException(SQLException e) {
                return new ExceptionMapper() { }.map(e);
            }
            @Override protected String query() {
                return text;
            }
            @Override protected void bindArgs(PreparedStatement stmt) { }
        };
    }

    public static <H> ResultSetMapper<Long, H> longMapper(H hints) {
        return new ResultSetMapper<>() {
            @Override public @Nullable H hints() {
                return hints;
            }
            @Override public Iterator<Long> map(ResultSet rs) {
                return new Iterator<>() {
                    @Override public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException e) {
                            throw new SqlExceptionR(e);
                        }
                    }
                    @Override public Long next() {
                        try {
                            return rs.getLong(1);
                        } catch (SQLException e) {
                            throw new SqlExceptionR(e);
                        }
                    }
                };
            }
        };
    }

    public static <H> ResultSetMapper<Long, H> badMapper(H hints) {
        return new ResultSetMapper<>() {
            @Override public @Nullable H hints() {
                return hints;
            }
            @Override public Iterator<Long> map(ResultSet rs) {
                throw new SqlExceptionR(new SQLException("oops"));
            }
        };
    }

    public static final Statement BAD_QUERY = queryStmt("absurd");

    @Configuration(
        checks = @CompileTimeChecks(
            databaseUrl = "jdbc:hsqldb:file:db1",
            // TrueSql.MainConnection.databaseUrl=xxx
            // TrueSql.MainConnection.username=aaaa
            // TrueSql.MainConnection.password=bbb
            // TrueSql.MainConnection.connectionInfo.шакал_видновс_настройка=FALSE
            connectionProperties = {
                @Property(key = "шакал_виндовс_настройка", value = "ТРУ")
            }
        ),
        exceptionMapper = ExceptionMapper.class,
        typeBindings = {
            @TypeBinding(
                sqlType = "enum1", javaClass = Enum1.class, rw = PgEnumBinding.class
            ),
            @TypeBinding(
                sqlType = "enum2", javaClass = Enum2.class, rw = PgEnumBinding.class
            )
        }
    )
    public record MainConnection(Connection w) implements ConnectionW { }

    public interface TestCode<E extends Exception> {
        void run(MainConnection connection) throws E;
    }

    public static final AtomicInteger dbIndex = new AtomicInteger(0);

    public record Options(boolean stmtCloseException) { }

    public static <E extends Exception> void withConnection(TestCode<E> code) throws SQLException, E {
        withConnection(new Options(false), code);
    }

    public static <E extends Exception> void withConnection(
        Options options, TestCode<E> code
    ) throws SQLException, E {
        var opened = new ArrayList<PreparedStatement>();
        var jdbcConnection =
//            DriverManager.getConnection(
//                "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234");
            DriverManager.getConnection(
                "jdbc:hsqldb:mem:db" + dbIndex.incrementAndGet(), "SA", ""
            );

        var connection = new MainConnection(
            (Connection) Proxy.newProxyInstance(
                Fixture.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    try {
                        var result = method.invoke(jdbcConnection, args);
                        if (
                            method.getName().equals("prepareStatement") ||
                            method.getName().equals("prepareCall")
                        ) {
                            opened.add((PreparedStatement) result);

                            return Proxy.newProxyInstance(
                                Fixture.class.getClassLoader(),
                                new Class[]{java.sql.PreparedStatement.class},
                                (proxy2, method2, args2) -> {
                                    try {
                                        if (
                                            options.stmtCloseException &&
                                            method2.getName().equals("close")
                                        ) {
                                            method2.invoke(result, args2);
                                            throw new SQLException("oops");
                                        } else {
                                            return method2.invoke(result, args2);
                                        }
                                    } catch (InvocationTargetException e) {
                                        throw e.getCause();
                                    }
                                }
                            );

                        }

                        return result;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            )
        );

        // TODO: create test fixture: t1, p1, f1
        connection.w.createStatement().execute("""
            create table t1(id bigint, v varchar(64));
            """);
        connection.w.createStatement().execute("""
            insert into t1(id, v) values(1, 'a');
            insert into t1(id, v) values(2, 'b');
            """);

        code.run(connection);

        for (var stmt : opened)
            Assertions.assertTrue(stmt.isClosed());
    }

    public enum Enum1 {
        X, Y
    }
    public enum Enum2 {
        A, B
    }
    public static class PgEnumBinding implements TypeReadWrite {
        @Override public String generateRead(
            String sqlType, String javaClass, String source, int columnNumber
        ) {
            return STR."\{javaClass}.valueOf(\{source}.getString(\{columnNumber}))";
        }

        // TODO: may cast: ResultSet -> PgResultSet
        // TODO: enum: dest: PSTMT, CALL, RS
        @Override public String generateWrite(
            String sqlType, String javaClass, String destination, int columnNumber, String value
        ) {
            return STR."\{destination}.setObject(\{columnNumber}, \{value}, java.sql.SqlType.Other))";
        }
    }
}
