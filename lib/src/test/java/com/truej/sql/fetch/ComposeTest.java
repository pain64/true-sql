package com.truej.sql.fetch;

import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.fetch.FetcherStream;
import com.truej.sql.v3.fetch.ResultSetMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ComposeTest {

    @Test void updateCountAnd() throws Exception {
        Fixture.withDataSource(ds -> {
            var query = Fixture
                .queryStmt(ds, "insert into t1 values(42, 'x')")
                .asGeneratedKeys("id");

            try (
                var result = query.fetchUpdateCount(
                    new FetcherStream<>(Fixture.longMapper())
                ).autoClosable()
            ) {
                Assertions.assertEquals(result.updateCount, 1L);
                Assertions.assertEquals(result.value.toList(), List.of(42L));
            }

        });

        Fixture.withDataSource(ds -> {
            var query = Fixture
                .queryStmt(ds, "insert into t1 values(42, 'x')")
                .asGeneratedKeys("id");

            var result2 = query.fetchUpdateCount(
                new FetcherList<>(Fixture.longMapper())
            );

            Assertions.assertEquals(result2.updateCount, 1L);
            Assertions.assertEquals(result2.value, List.of(42L));
        });
    }

    <T> T map(ResultSet rs, Class<T> toClass) {
        if(toClass == String.class)
            return (T) "42";

        throw new RuntimeException("unreachable");
    }

    @Test void generatedKeysAndList() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "insert into t1 values(42, 'v')")
                .asGeneratedKeys("id");
            Assertions.assertEquals(
                42L, query.fetchOne(Fixture.longMapper())
            );
        });

        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "insert into t1 values(42, 'v')")
                .asGeneratedKeys("id");
            Assertions.assertEquals(
                42L, query.fetchOne(Fixture.longMapper())
            );
        });
    }
}
