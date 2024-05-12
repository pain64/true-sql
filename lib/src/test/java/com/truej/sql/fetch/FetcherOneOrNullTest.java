package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherOneOrNull;
import com.truej.sql.v3.fetch.TooMuchRowsException;
import com.truej.sql.v3.prepare.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class FetcherOneOrNullTest {
    @Test void fetchOneOrNull() throws SQLException {
        Fixture.withDataSource(ds -> {

            Assertions.assertEquals(
                FetcherOneOrNull.fetch(
                    Transform.value(),
                    Fixture.queryStmt(ds, "select id from t1 where id = 1"),
                    Fixture.longMapper()
                ),
                1L
            );

            Assertions.assertNull(
                FetcherOneOrNull.fetch(
                    Transform.value(),
                    Fixture.queryStmt(ds, "select id from t1 where id = 777"),
                    Fixture.longMapper()
                )
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    FetcherOneOrNull.fetch(
                        Transform.value(),
                        Fixture.queryStmt(ds, "select id from t1"),
                        Fixture.longMapper()
                    )
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherOneOrNull.fetch(
                        Transform.value(),
                        Fixture.badQuery(ds),
                        Fixture.longMapper()
                    )
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherOneOrNull.fetch(
                        Transform.value(),
                        Fixture.queryStmt(ds, "select id from t1 where id = 1"),
                        Fixture.badMapper()
                    )
            );
        });
    }
}
