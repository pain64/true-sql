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
                Fixture.queryStmt("select id from t1 where id = 1")
                    .fetchOneOrNull(ds, Fixture.longMapper(null)),
                1L
            );

            Assertions.assertNull(
                Fixture.queryStmt("select id from t1 where id = 777")
                    .fetchOneOrNull(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt("select id from t1")
                        .fetchOneOrNull(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchOneOrNull(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt("select id from t1 where id = 1")
                        .fetchOneOrNull(ds, Fixture.badMapper(null))
            );
        });
    }
}
