package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
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

//            var r1 = FetcherList.fetch(Transform.noOp(), query, Fixture.longMapper(), 0);
//            var r2 = FetcherList.fetch(Transform.updateCount(), query, Fixture.longMapper(), 0);

            Assertions.assertEquals(
                FetcherList.fetch(Transform.value(), query, Fixture.longMapper(), 0), expected
            );

            Assertions.assertEquals(
                FetcherList.fetch(Transform.value(),query, Fixture.longMapper(), 2), expected
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherList.fetch(Transform.value(), Fixture.badQuery(ds), Fixture.longMapper(), 0)
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherList.fetch(Transform.value(), query, Fixture.badMapper(), 0)
            );
        });
    }
}
