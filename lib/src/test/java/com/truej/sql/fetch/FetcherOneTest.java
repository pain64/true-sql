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
                Fixture.queryStmt("select id from t1 where id = 1")
                    .fetchOne(ds, Fixture.longMapper(null)),
                1L
            );

            Assertions.assertThrows(
                TooFewRowsException.class, () ->
                    Fixture.queryStmt("select id from t1 where id = 777")
                        .fetchOne(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt("select id from t1")
                        .fetchOne(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchOne(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt("select id from t1 where id = 1")
                        .fetchOne(ds, Fixture.badMapper(null))
            );
        });
    }
}
