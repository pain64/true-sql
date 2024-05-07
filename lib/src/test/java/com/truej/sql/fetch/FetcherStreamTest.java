package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class FetcherStreamTest {
    @Test void fetchStream() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");
            try (var stream = query.fetchStream(Fixture.longMapper())) {
                Assertions.assertEquals(stream.toList(), List.of(1L, 2L));
            }

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.badQuery(ds).fetchStream(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    query.fetchStream(Fixture.badMapper())
            );
        });

        Fixture.withDataSource(new Fixture.Options(false, true), ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");
            Assertions.assertThrows(
                SqlExceptionR.class, () -> {
                    var stream = query.fetchStream(Fixture.longMapper());
                    stream.close(); // will throw
                }
            );
        });
    }
}
