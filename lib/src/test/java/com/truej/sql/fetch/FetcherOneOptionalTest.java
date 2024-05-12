package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherOneOptional;
import com.truej.sql.v3.fetch.TooMuchRowsException;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

public class FetcherOneOptionalTest {
    @Test void fetchOneOptional() throws SQLException {
        Fixture.withDataSource(ds -> {

            Assertions.assertEquals(
                FetcherOneOptional.fetch(
                    Transform.value(),
                    Fixture.queryStmt(ds, "select id from t1 where id = 1"),
                    Fixture.longMapper()
                ),
                Optional.of(1L)
            );

            Assertions.assertEquals(
                FetcherOneOptional.fetch(
                    Transform.value(),
                    Fixture.queryStmt(ds, "select id from t1 where id = 777"),
                    Fixture.longMapper()
                ),
                Optional.empty()
            );

            Assertions.assertThrows(
                TooMuchRowsException.class, () ->
                    FetcherOneOptional.fetch(
                        Transform.value(),
                        Fixture.queryStmt(ds, "select id from t1"),
                        Fixture.longMapper()
                    )
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherOneOptional.fetch(
                        Transform.value(),
                        Fixture.badQuery(ds), Fixture.longMapper()
                    )
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherOneOptional.fetch(
                        Transform.value(),
                        Fixture.queryStmt(ds, "select id from t1 where id = 1"), Fixture.badMapper()
                    )
            );
        });
    }
}
