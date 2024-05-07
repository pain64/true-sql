package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.TooFewRowsException;
import com.truej.sql.v3.fetch.TooMuchRowsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class FetcherOneTest {
    @Test void fetchOne() throws SQLException {
        Fixture.withDataSource(ds -> {

            Assertions.assertEquals(
                Fixture.queryStmt(ds, "select id from t1 where id = 1")
                    .fetchOne(Fixture.longMapper()),
                1L
            );

            Assertions.assertThrows(
                TooFewRowsException.class, () ->
                    Fixture.queryStmt(ds, "select id from t1 where id = 777")
                        .fetchOne(Fixture.longMapper())
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt(ds, "select id from t1")
                        .fetchOne(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.badQuery(ds).fetchOne(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt(ds, "select id from t1 where id = 1")
                        .fetchOne(Fixture.badMapper())
            );
        });
    }
}
