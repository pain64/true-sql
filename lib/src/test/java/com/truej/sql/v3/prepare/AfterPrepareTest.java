package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.source.Source;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AfterPrepareTest {
    <S, P extends PreparedStatement, R, U> void testSuite(
        Base<S, P, R, U> query
    ) throws SQLException {
        FetcherNone.fetch(Transform.value(), query);
    }
    @Test void afterPrepareConfig() throws SQLException {
        Fixture.withDataSource(ds -> {
            testSuite(new Statement<>() {
                @Override protected Source source() { return ds; }
                @Override protected PreparedStatement prepare(Connection c) throws SQLException {
                    return c.prepareStatement("select id from t1");
                }
                @Override protected void afterPrepare(PreparedStatement stmt) throws SQLException {
                    stmt.setFetchSize(9000);
                    stmt.setMaxFieldSize(9000);
                }
                @Override protected void bindArgs(PreparedStatement stmt) { }
            });
        });
    }
}
