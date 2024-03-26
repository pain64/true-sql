package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class FetcherListTest {
    @Test void fetchList() throws SQLException {
        Fixture.withConnection(connection -> {
            var query = Fixture.queryStmt("select id from t1");
            var expected = List.of(1L, 2L);

            Assertions.assertEquals(
                query.fetchList(connection, Fixture.longMapper(null)), expected
            );

            Assertions.assertEquals(
                query.fetchList(
                    connection,
                    Fixture.longMapper(new FetcherList.Hints().expectedSize(2))
                ),
                expected
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchList(connection, Fixture.longMapper(null))
            );

            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    query.fetchList(connection, Fixture.badMapper(null))
            );
        });
    }
}
