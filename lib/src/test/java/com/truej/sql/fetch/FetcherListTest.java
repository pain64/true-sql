package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class FetcherListTest {
    // Statement+, Call-, BatchStatement+, BatchCall-
    @Test void fetchList() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");
            var expected = List.of(1L, 2L);

            Assertions.assertEquals(
                query.fetchList(Fixture.longMapper()), expected
            );

            Assertions.assertEquals(
                query.fetchList(Fixture.longMapper(), 2), expected
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.badQuery(ds).fetchList(Fixture.longMapper())
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    query.fetchList(Fixture.badMapper())
            );
        });
    }
}
