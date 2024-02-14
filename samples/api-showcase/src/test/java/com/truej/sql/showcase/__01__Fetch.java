package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.*;

public class __01__Fetch {
    @Test void one(DataSource ds) {
        assertEquals(
            Stmt. "select name from users where id = \{ 42 }" .fetchOne(ds, String.class)
            , "Joe"
        );
    }

    @Test void oneOrNull(DataSource ds) {
        assertNull(Stmt. "select name from users where id = \{ 1 }" .fetchOneOrNull(ds, String.class));
    }

    @Test void oneOptional(DataSource ds) {
        assertEquals(
            Stmt. "select name from users where id = \{ 1 }" .fetchOneOptional(ds, String.class)
            , Optional.empty()
        );
    }

    @Test void none(DataSource ds) {
        Stmt."insert into users values(1, 'John', 'xxx@email.com')".fetchNone(ds);
    }

    @Test void array(DataSource ds) {
        assertArrayEquals(
            Stmt."select name from users".fetchArray(ds, String.class)
            , List.of("Ivan", "Joe").toArray()
        );
    }

    @Test void list(DataSource ds) {
        assertEquals(
            Stmt."select name from users".fetchList(ds, String.class)
            , List.of("Ivan", "Joe")
        );
    }

    @Test void stream(DataSource ds) {
        assertEquals(
            Stmt."select name from users".fetchStream(ds, String.class)
                .toList(), List.of("Ivan", "Joe")
        );
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
