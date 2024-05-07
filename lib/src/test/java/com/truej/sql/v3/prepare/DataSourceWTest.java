package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import com.truej.sql.v3.SqlExceptionR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

public class DataSourceWTest {
    @Test void withConnection() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertEquals(
                ds.withConnection(cn ->
                    Fixture.queryStmt(cn, "values(1)")
                        .fetchList(Fixture.longMapper())
                ), List.of(1L)
            )
        );
    }

    @Test void withConnectionLambdaThrows() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    ds.withConnection(cn -> {
                        throw new SQLException("");
                    })
            )
        );
    }

    @Test void withConnectionGetConnectionThrows() throws SQLException {
        Fixture.withDataSource(new Fixture.Options(true, false), ds ->
            Assertions.assertThrows(
                SqlExceptionR.class, () -> ds.withConnection(cn -> "not used")
            )
        );
    }

    @Test void inTransaction() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertEquals(
                ds.inTransaction(cn ->
                    Fixture.queryStmt(cn, "values(1)")
                        .fetchList(Fixture.longMapper())
                ), List.of(1L)
            )
        );
    }
}
