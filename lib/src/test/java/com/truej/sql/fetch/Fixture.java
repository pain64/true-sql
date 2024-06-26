package com.truej.sql.fetch;

import com.truej.sql.v3.ConstraintViolationException;
import com.truej.sql.v3.prepare.BatchStatement;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.config.*;
import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.prepare.Statement;
import com.truej.sql.v3.source.DataSourceW;
import com.truej.sql.v3.source.Source;
import org.hsqldb.HsqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Fixture {
    public static Statement<PreparedStatement> queryStmt(Source source, String text) {
        return new Statement<>() {
            @Override protected Source source() { return source; }
            @Override protected PreparedStatement prepare(Connection connection) throws SQLException {
                return connection.prepareStatement(text);
            }
            @Override protected void bindArgs(PreparedStatement stmt) { }
        };
    }

    public static Statement<PreparedStatement> queryStmtGeneratedKeys(Source source, String text, String... columnNames) {
        return new Statement<>() {
            @Override protected Source source() { return source; }
            @Override protected PreparedStatement prepare(Connection connection) throws SQLException {
                return connection.prepareStatement(text, columnNames);
            }
            @Override protected boolean isAsGeneratedKeys() { return true; }
            @Override protected void bindArgs(PreparedStatement stmt) { }
        };
    }

    public static BatchStatement<PreparedStatement> queryBatchStmt(Source source, String text) {
        return new BatchStatement<>() {
            @Override protected Source source() { return source; }
            @Override protected PreparedStatement prepare(Connection connection) throws SQLException {
                return connection.prepareStatement(text);
            }
            @Override protected void bindArgs(PreparedStatement stmt) { }
        };
    }

    public static ResultSetMapper<Long> longMapper() {
        return rs -> new Iterator<>() {
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

    public static ResultSetMapper<Long> badMapper() {
        return _ -> {
            throw new SqlExceptionR(new SQLException("oops"));
        };
    }

    public static Statement<PreparedStatement> badQuery(Source source) {
        return queryStmt(source, "absurd");
    }

    @Configuration(
        checks = @CompileTimeChecks(
            url = "jdbc:hsqldb:file:db1"
            // TrueSql.MainConnection.databaseUrl=xxx
            // TrueSql.MainConnection.username=aaaa
            // TrueSql.MainConnection.password=bbb
        )
//        typeBindings = {
//            @TypeBinding(
//                sqlType = "enum1", javaClass = Enum1.class, rw = PgEnumBinding.class
//            ),
//            @TypeBinding(
//                sqlType = "enum2", javaClass = Enum2.class, rw = PgEnumBinding.class
//            )
//        }
    )
    public record MainDataSource(DataSource w) implements DataSourceW {
        @Override public RuntimeException mapException(SQLException ex) {

            if (ex instanceof SQLIntegrityConstraintViolationException iex) {
                var hex = (HsqlException) iex.getCause();
                var parts = hex.getMessage().split(";");
                var constraintAndTable = parts[parts.length - 1].split("table:");

                return new ConstraintViolationException(
                    constraintAndTable[1].trim(),
                    constraintAndTable[0].trim()
                );
            }

            return DataSourceW.super.mapException(ex);
        }
    }

    public interface TestCode<E extends Exception> {
        void run(MainDataSource ds) throws E;
    }

    public static final AtomicInteger dbIndex = new AtomicInteger(0);

    public record Options(boolean getConnectionException, boolean stmtCloseException) { }

    public static <E extends Exception> void withDataSource(TestCode<E> code) throws SQLException, E {
        withDataSource(new Options(false, false), code);
    }

    public static <E extends Exception> void withDataSource(
        Options options, TestCode<E> code
    ) throws SQLException, E {

        var openedConnections = new ArrayList<Connection>();
        var openedStmt = new ArrayList<PreparedStatement>();
        var ds = new JDBCDataSource() {
            boolean isInit = true;

            {
                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:hsqldb:mem:db" + dbIndex.incrementAndGet());
                setUser("SA");
                setPassword("");

                try (var initConn = this.getConnection()) {
                    // TODO: create test fixture: t1, p1, f1
                    initConn.createStatement().execute("""
                create table t1(id bigint, v varchar(64));
                """);
                    initConn.createStatement().execute("""  
                alter table t1 add constraint t1_pk primary key (id);
                """);
                    initConn.createStatement().execute("""
                insert into t1(id, v) values(1, 'a');
                insert into t1(id, v) values(2, 'b');
                """);
                    initConn.createStatement().execute("""
                create procedure p1(in x int, inout r int)
                  begin atomic
                     set r = x * 2;
                  end
                """);
                }

                isInit = false;
            }

            @Override public Connection getConnection() throws SQLException {
                if (!isInit && options.getConnectionException)
                    throw new SQLException("emulation of get connection exception");

                var jdbcConnection = super.getConnection();
                openedConnections.add(jdbcConnection);

                return (Connection) Proxy.newProxyInstance(
                    Fixture.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        try {
                            var result = method.invoke(jdbcConnection, args);
                            if (
                                method.getName().equals("prepareStatement") ||
                                method.getName().equals("prepareCall")
                            ) {
                                var targetClass = method.getName().equals("prepareStatement")
                                    ? PreparedStatement.class : CallableStatement.class;
                                openedStmt.add((PreparedStatement) result);

                                return Proxy.newProxyInstance(
                                    Fixture.class.getClassLoader(),
                                    new Class[]{targetClass},
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
                );
            }
        };

        code.run(new MainDataSource(ds));

        for (var connection : openedConnections)
            Assertions.assertTrue(connection.isClosed());

        for (var stmt : openedStmt)
            Assertions.assertTrue(stmt.isClosed());
    }

    public enum Enum1 {
        X, Y
    }
    public enum Enum2 {
        A, B
    }
//    public static class PgEnumBinding implements TypeReadWrite {
//        @Override public String generateRead(
//            String sqlType, String javaClass, String source, int columnNumber
//        ) {
//            return STR."\{javaClass}.valueOf(\{source}.getString(\{columnNumber}))";
//        }
//
//        // TODO: may cast: ResultSet -> PgResultSet
//        // TODO: enum: dest: PSTMT, CALL, RS
//        @Override public String generateWrite(
//            String sqlType, String javaClass, String destination, int columnNumber, String value
//        ) {
//            return STR."\{destination}.setObject(\{columnNumber}, \{value}, java.sql.SqlType.Other))";
//        }
//    }
}
