package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.truej.sql.v3.prepare.Parameters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {
    void test(MainDataSource ds) {
        assertEquals(
            ds."insert into users(name, email) values('John', 'xxx@email.com')"
                .asGeneratedKeys("id")
                //.g.withUpdateCount().g.fetchStream(Long.class)
                .withUpdateCount.g.fetchStream(Long.class)
            //.withUpdateCount.g.fetchStream(Long.class)
            //.updateCount.g.fetchStream(Long.class)
            //.fetchUpdateCount(new FetcherStream<>(Long.class))
            , 1L
        );

        ds."select v from t1 where id = \{42}".fetchOne(String.class);
        ds."\{out()} = call p1(1, \{inout(2)})"
            .asCall().fetchNone();

        ds."select v from t1 where (id, v) in = (\{
            unfold2(List.of(new Pair<>(1, "a"), new Pair<>(2, "b")))
            })"
            .fetchList(String.class);
    }

    void inClauseTest(MainDataSource ds) {
        var ids = List.of(1, 2, 3);
        ds."select * from users where id in ( \{unfold(ids)} )"
            .fetchNone();

        assertEquals(
            ds."insert into users(name, email) values('John', 'xxx@email.com')"
                .asGeneratedKeys("id")
                //.g.withUpdateCount().g.fetchStream(Long.class)
                .withUpdateCount.g.fetchStream(Long.class)
            //.withUpdateCount.g.fetchStream(Long.class)
            //.updateCount.g.fetchStream(Long.class)
            //.fetchUpdateCount(new FetcherStream<>(Long.class))
            , 1L
        );
    }
}
