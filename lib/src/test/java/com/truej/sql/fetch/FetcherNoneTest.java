package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class FetcherNoneTest {
    @Test void fetchNone() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "insert into t1 values(43, 'a')");

            Assertions.assertNull(FetcherNone.fetch(Transform.value(), query));

            Assertions.assertThrows(
                SqlExceptionR.class, () -> FetcherNone.fetch(Transform.value(), Fixture.badQuery(ds))
            );
        });
    }
}
