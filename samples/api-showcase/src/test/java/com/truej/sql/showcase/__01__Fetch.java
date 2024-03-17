package com.truej.sql.showcase;

import com.truej.sql.v3.fetch.FetcherList;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.truej.sql.v3.TrueSql.Stmt;
import static com.truej.sql.v3.TrueSql.m;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __01__Fetch {

    @Test void one(DataSource ds) {
        assertEquals(
            Stmt. "select name from users where id = \{ 42 }"
                .fetchOne(ds, m(String.class))
            , "Joe"
        );
    }

    @Test void oneOrNull(DataSource ds) {
        assertNull(
            Stmt. "select name from users where id = \{ 1 }"
                .fetchOneOrNull(ds, m(String.class))
        );
    }

    @Test void oneOptional(DataSource ds) {
        assertEquals(
            Stmt. "select name from users where id = \{ 1 }"
                .fetchOneOptional(ds, m(String.class))
            , Optional.empty()
        );
    }

    @Test void none(DataSource ds) {
        Stmt."insert into users values(1, 'John', 'xxx@email.com')"
            .fetchNone(ds);
    }

    @Test void array(DataSource ds) {
        assertEquals(
            Stmt."select name from users"
                .fetchArray(ds, m(String.class))
            , List.of("Ivan", "Joe").toArray()
        );
    }

    @Test void list(DataSource ds) {
        assertEquals(
            Stmt."select name from users"
                .fetchList(ds, m(String.class))
            , List.of("Ivan", "Joe")
        );

        assertEquals(
            Stmt."select name from users".fetchList(
                ds, m(String.class, new FetcherList.Hints(10))
            )
            , List.of("Ivan", "Joe")
        );

        assertEquals(
            Stmt."select name from users".fetchList(
                ds, m(String.class, new FetcherList.Hints().expectedSize(10)))
            , List.of("Ivan", "Joe")
        );
    }

    @Test void stream(DataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = Stmt."select name from users"
                .fetchStream(ds, m(String.class))
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    @Test void updateCount(DataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = Stmt."upda"
                .fetchStream(ds, m(String.class))
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    @Test void updateCountAndNone(DataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = Stmt."select name from users"
                .fetchStream(ds, m(String.class))
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    @Test void updateCountAndStream(DataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = Stmt."select name from users"
                .fetchStream(ds, m(String.class))
        ) {
            // stream is lazy, we can iterate over
            // stream.forEach(System.out::println);
            assertEquals(
                stream.toList(), List.of("Ivan", "Joe")
            );
        }
    }

    @Test void manual(DataSource ds) throws SQLException {
        assertEquals(
            Stmt."select name from users".fetch(ds, stmt -> {
                var rs = stmt.getResultSet();
                rs.next();
                return rs.getString(1);
            })
            , "some"
        );
    }
}
