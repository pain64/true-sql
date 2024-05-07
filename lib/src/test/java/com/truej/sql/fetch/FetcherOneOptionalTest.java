package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.TooMuchRowsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

public class FetcherOneOptionalTest {
    @Test void fetchOneOptional() throws SQLException {
        Fixture.withDataSource(ds -> {

            Assertions.assertEquals(
                Fixture.queryStmt(ds, "select id from t1 where id = 1")
                    .fetchOneOptional(Fixture.longMapper()),
                Optional.of(1L)
            );

            Assertions.assertEquals(
                Fixture.queryStmt(ds, "select id from t1 where id = 777")
                    .fetchOneOptional(Fixture.longMapper()),
                Optional.empty()
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt(ds, "select id from t1")
                        .fetchOneOptional(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.badQuery(ds).fetchOneOptional(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt(ds, "select id from t1 where id = 1")
                        .fetchOneOptional(Fixture.badMapper())
            );
        });
    }
}
