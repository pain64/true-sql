package com.truej.sql.showcase;

import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.prepare.ManagedAction;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.truej.sql.v3.TrueSql.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __01__Fetch {

    @Test void one(MainDataSource ds) {
        assertEquals(
            ds."select name from users where id = \{42}"
                .fetchOne(String.class)
            , "Joe"
        );
    }

    @Test void oneOrNull(MainDataSource ds) {
        assertNull(
            ds."select name from users where id = \{1}"
                .fetchOneOrNull(String.class)
        );
    }

    @Test void oneOptional(MainDataSource ds) {
        assertEquals(
            ds."select name from users where id = \{1}"
                .fetchOneOptional(String.class)
            , Optional.empty()
        );
    }

    @Test void none(MainDataSource ds) {
        ds."insert into users values(1, 'John', 'xxx@email.com')"
            .fetchNone();
    }

    @Test void list(MainDataSource ds) {
        assertEquals(
            ds."select name from users".fetchList(String.class)
            , List.of("Ivan", "Joe")
        );

        assertEquals(
            ds."select name from users".fetchList(
                String.class, new FetcherList.Hints().expectedSize(10)
            )
            , List.of("Ivan", "Joe")
        );
    }

    @Test void stream(MainDataSource ds) {
        // NB: stream must be closed!
        try (
            var stream = ds."select name from users"
                .fetchStream(String.class)
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
            var stream = ds."upda"
                .fetchStream(String.class)
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
            var stream = ds."select name from users"
                .fetchStream(String.class)
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
            var stream = ds."select name from users"
                .fetchStream(String.class)
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
            ds."select name from users".fetch(
                new ManagedAction<>() {
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
