package com.truej.sql.showcase;

import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.prepare.ManagedAction;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.truej.sql.v3.TrueSql.Stmt;
import static com.truej.sql.v3.TrueSql.m;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __01__Fetch {

    @Test void one(MainDataSource ds) {
        assertEquals(
            Stmt."select name from users where id = \{42}"
                .fetchOne(ds, m(String.class))
            , "Joe"
        );
    }

    @Test void oneOrNull(MainDataSource ds) {
        assertNull(
            Stmt."select name from users where id = \{1}"
                .fetchOneOrNull(ds, m(String.class))
        );
    }

    @Test void oneOptional(MainDataSource ds) {
        assertEquals(
            Stmt."select name from users where id = \{1}"
                .fetchOneOptional(ds, m(String.class))
            , Optional.empty()
        );
    }

    @Test void none(MainDataSource ds) {
        Stmt."insert into users values(1, 'John', 'xxx@email.com')"
            .fetchNone(ds);
    }

    @Test void list(MainDataSource ds) {
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

    @Test void stream(MainDataSource ds) {
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

    @Test void updateCount(MainDataSource ds) {
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

    @Test void updateCountAndNone(MainDataSource ds) {
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

    @Test void updateCountAndStream(MainDataSource ds) {
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

    @Test void manual(MainDataSource ds) {
        assertEquals(
            Stmt."select name from users".fetch(
                ds, new ManagedAction.Full<>() {
                    @Override public boolean willStatementBeMoved() {
                        return false;
                    }
                    @Override public Object apply(
                        Void executionResult, PreparedStatement stmt
                    ) throws SQLException {
                        var rs = stmt.getResultSet();
                        rs.next();
                        return rs.getString(1);
                    }
                }
            )
            , "some"
        );
    }
}
