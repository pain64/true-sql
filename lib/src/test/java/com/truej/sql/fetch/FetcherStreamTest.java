package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherStream;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class FetcherStreamTest {
    @Test void fetchStream() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");
            try (var stream = FetcherStream.fetch(Transform.value(), query, Fixture.longMapper())) {
                Assertions.assertEquals(stream.toList(), List.of(1L, 2L));
            }

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherStream.fetch(Transform.value(), Fixture.badQuery(ds), Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherStream.fetch(Transform.value(), query, Fixture.badMapper())
            );
        });

        Fixture.withDataSource(new Fixture.Options(false, true), ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");
            Assertions.assertThrows(
                SqlExceptionR.class, () -> {
                    var stream = FetcherStream.fetch(Transform.value(), query, Fixture.longMapper());
                    stream.close(); // will throw
                }
            );
        });
    }
}
