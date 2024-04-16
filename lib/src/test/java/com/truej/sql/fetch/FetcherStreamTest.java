package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class FetcherStreamTest {
    @Test void fetchStream() throws SQLException {
        var query = Fixture.queryStmt("select id from t1");
        Fixture.withDataSource(ds -> {
            try (var stream = query.fetchStream(ds, Fixture.longMapper(null))) {
                Assertions.assertEquals(stream.toList(), List.of(1L, 2L));
            }

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchStream(ds, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    query.fetchStream(ds, Fixture.badMapper(null))
            );
        });

        Fixture.withDataSource(new Fixture.Options(false, true), ds ->
            Assertions.assertThrows(
                SqlExceptionR.class, () -> {
                    var stream = query.fetchStream(ds, Fixture.longMapper(null));
                    stream.close(); // will throw
                }
            ));
    }
}
