package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import com.truej.sql.v3.source.Source;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AfterPrepareTest {
    <S, P extends PreparedStatement, R, U> void testSuite(
        Base<S, P, R, U> query
    ) throws SQLException {
        query.afterPrepare(stmt -> {
            stmt.setFetchSize(9000);
            stmt.setMaxFieldSize(9000);
        });
        query.fetchNone();
    }
    @Test void afterPrepareConfig() throws SQLException {
        Fixture.withDataSource(ds -> {
            testSuite(new Statement() {
                @Override protected Source source() { return ds; }
                @Override protected String query() {
                    return "select id from t1";
                }
                @Override protected void bindArgs(PreparedStatement stmt) { }
            });
        });
        
        Fixture.withDataSource(ds -> {
            testSuite(new BatchStatement() {
                @Override protected Source source() { return ds; }
                @Override protected String query() {
                    return "update t1 set v = 'xxx' where id = ?";
                }
                @Override protected void bindArgs(PreparedStatement stmt) throws SQLException {
                    stmt.setLong(1, 42);
                    stmt.addBatch();
                }
            });
        });

        Fixture.withDataSource(ds -> {
            testSuite(new Call() {
                @Override protected Source source() { return ds; }
                @Override protected String query() {
                    return "call p1(?, ?)";
                }
                @Override protected void bindArgs(CallableStatement stmt) throws SQLException {
                    stmt.setInt(1, 42);
                    stmt.setInt(2, 0);
                }
            });
        });

        Fixture.withDataSource(ds -> {
            testSuite(new BatchCall() {
                @Override protected Source source() { return ds; }
                @Override protected String query() {
                    return "call p1(?, ?)";
                }
                @Override protected void bindArgs(CallableStatement stmt) throws SQLException {
                    stmt.setInt(1, 42);
                    stmt.setInt(2, 0);
                    stmt.addBatch();
                    stmt.setInt(1, 43);
                    stmt.setInt(2, 0);
                    stmt.addBatch();
                }
            });
        });
    }
}
