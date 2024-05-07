package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.TooMuchRowsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class FetcherOneOrNullTest {
    @Test void fetchOneOrNull() throws SQLException {
        Fixture.withDataSource(ds -> {

            Assertions.assertEquals(
                Fixture.queryStmt(ds, "select id from t1 where id = 1")
                    .fetchOneOrNull(Fixture.longMapper()),
                1L
            );

            Assertions.assertNull(
                Fixture.queryStmt(ds, "select id from t1 where id = 777")
                    .fetchOneOrNull(Fixture.longMapper())
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt(ds, "select id from t1")
                        .fetchOneOrNull(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.badQuery(ds).fetchOneOrNull(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt(ds, "select id from t1 where id = 1")
                        .fetchOneOrNull(Fixture.badMapper())
            );
        });
    }
}
