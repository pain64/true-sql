package com.truej.sql.fetch;

import com.truej.sql.v3.fetch.FetcherGeneratedKeys;
import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.fetch.FetcherStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ComposeTest {

    @Test void updateCountAndGeneratedKeys() throws Exception {
        var query = Fixture
            .queryStmt("insert into t1 values(42, 'x')")
            .withGeneratedKeys("id");
        Fixture.withDataSource(ds -> {

            try (
                var result = query.fetchUpdateCount(
                    ds, new FetcherGeneratedKeys<>(
                        new FetcherStream<>(Fixture.longMapper(null))
                    )
                ).autoClosable()
            ) {
                Assertions.assertEquals(result.updateCount, 1L);
                Assertions.assertEquals(result.value.toList(), List.of(42L));
            }

        });

        Fixture.withDataSource(ds -> {
            var result2 = query.fetchUpdateCount(
                ds, new FetcherGeneratedKeys<>(
                    new FetcherList<>(Fixture.longMapper(null))
                )
            );

            Assertions.assertEquals(result2.updateCount, 1L);
            Assertions.assertEquals(result2.value, List.of(42L));
        });
    }
//
//    @Test void generatedKeysAndArray() throws SQLException {
//        Fixture.withConnection(connection -> {
//            var query = Fixture
//                .queryStmt("update t1 set v = 'x'")
//                .withGeneratedKeys("id");
//
//            Assertions.assertArrayEquals(
//                query.fetchGeneratedKeys(
//                    connection, new FetcherArray<>(Fixture.longMapper(null))
//                ), new Long[]{1L, 2L});
//        });
//    }
}
