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
                Fixture.queryStmt("select id from t1 where id = 1")
                    .fetchOneOptional(ds, Fixture.longMapper(null)),
                Optional.of(1L)
            );

            Assertions.assertEquals(
                Fixture.queryStmt("select id from t1 where id = 777")
                    .fetchOneOptional(ds, Fixture.longMapper(null)),
                Optional.empty()
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    Fixture.queryStmt("select id from t1")
                        .fetchOneOptional(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchOneOptional(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.queryStmt("select id from t1 where id = 1")
                        .fetchOneOptional(ds, Fixture.badMapper(null))
            );
        });
    }
}
