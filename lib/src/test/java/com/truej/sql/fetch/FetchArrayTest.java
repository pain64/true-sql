package com.truej.sql.fetch;

import com.truej.sql.util.TestDataSource;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherArray;
import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.prepare.Statement;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class FetchArrayTest {
    private final Statement queryStmt = new Statement() {
        @Override public String query() {
            return "select id from t1";
        }
        @Override public void bindArgs(PreparedStatement stmt) { }
    };

    private <H> ResultSetMapper<Long, H> longMapper(H hints) {
        return new ResultSetMapper<>() {
            @Override public Class<Long> tClass() {
                return Long.class;
            }
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

    @Test void fetchArray() throws SQLException {
        var connection = DriverManager.getConnection("jdbc:hsqldb:mem:db", "SA", "");

        // TODO: create test fixture: t1, p1, f1
        connection.createStatement().execute("""
            create table t1(id bigint, v varchar(64));
            """);
        connection.createStatement().execute("""
            insert into t1 values(1, 'a');
            insert into t1 values(2, 'b');
            """);

        var expected =  new Long[]{ 1L, 2L };

        // TODO: check exception wrapped: SQLException -> SQLExceptionR or ConstraintViolationException

        Assertions.assertArrayEquals(
            queryStmt.fetchArray(connection, longMapper(null)), expected
        );

        Assertions.assertArrayEquals(
            queryStmt.fetchArray(
                connection,
                longMapper(new FetcherArray.Hints().expectedSize(2))
            ),
            expected
        );

        var ds = new TestDataSource(connection);

        Assertions.assertArrayEquals(
            queryStmt.fetchArray(ds, longMapper(null)), expected
        );

        Assertions.assertTrue(ds.wrapped.isClosed());
    }
}
