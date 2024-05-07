package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class FetcherNoneTest {
    @Test void fetchNone() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "insert into t1 values(43, 'a')");

            Assertions.assertNull(query.fetchNone());

            Assertions.assertThrows(
                SqlExceptionR.class, () -> Fixture.badQuery(ds).fetchNone()
            );
        });
    }
}
