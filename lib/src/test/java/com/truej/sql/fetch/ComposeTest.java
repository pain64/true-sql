package com.truej.sql.fetch;

import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.fetch.FetcherStream;
import com.truej.sql.v3.prepare.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class ComposeTest {

    @Test void updateCountAnd() throws Exception {
        var query = Fixture
            .queryStmt("insert into t1 values(42, 'x')")
            .withGeneratedKeys("id");
        Fixture.withDataSource(ds -> {

            try (
                var result = query.fetchUpdateCount(
                    ds, new FetcherStream<>(Fixture.longMapper(null))
                ).autoClosable()
            ) {
                Assertions.assertEquals(result.updateCount, 1L);
                Assertions.assertEquals(result.value.toList(), List.of(42L));
            }

        });

        Fixture.withDataSource(ds -> {
            var result2 = query.fetchUpdateCount(
                ds, new FetcherList<>(Fixture.longMapper(null))
            );

            Assertions.assertEquals(result2.updateCount, 1L);
            Assertions.assertEquals(result2.value, List.of(42L));
        });
    }

    void xxx(Statement query) throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertEquals(
                42L, query.fetchOne(ds, Fixture.longMapper(null))
            )
        );
    }

    @Test void generatedKeysAndList() throws SQLException {
        var query = Fixture.queryStmt("insert into t1 values(42, 'v')");
        xxx(query.withGeneratedKeys("id"));
        xxx(query.withGeneratedKeys(1));
    }
}
