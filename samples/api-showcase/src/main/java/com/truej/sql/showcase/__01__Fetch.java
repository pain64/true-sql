package com.truej.sql.showcase;

import com.truej.sql.v3.source.ConnectionW;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __01__Fetch {

    void one(MainDataSource ds) {
//  TODO: cool stuff
//      var x = ds."select name from users where id = \{42}"
//          .fetchOne(long.class);

        assertEquals(
            ds.q("select name from users where id = ?", 42)
                .fetchOne(String.class)
            , "Joe"
        );
    }

    void oneOrNull(MainDataSource ds) {
        assertNull(
            ds.q("select name from users where id = ?", 1)
                .fetchOneOrZero(String.class)
        );
    }

    void none(MainDataSource ds) {
        ds.q("insert into users values(1, 'John', 'xxx@email.com')")
            .fetchNone();
    }

    void list(MainDataSource ds) {
        assertEquals(
            ds.q("select name from users").fetchList(String.class)
            , List.of("Ivan", "Joe")
        );
    }

    void stream(MainDataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = ds.q("select name from users")
                .fetchStream(String.class)
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    void updateCount(MainDataSource ds) {
        ds.q("update users set name = 'Joe'").fetchNone();
    }

    void updateCountAndNone(MainDataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = ds.q("select name from users")
                .fetchStream(String.class)
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    void updateCountAndStream(ConnectionW cn) {
        try (
            var result = cn.q("update users set id = rand()")
                .asGeneratedKeys("id").withUpdateCount
                .fetchStream(Long.class)
        ) {
            assertEquals(result.updateCount, 2);
            assertEquals(
                result.value.toList(), List.of(1L, 2L)
            );
        }
    }
}
