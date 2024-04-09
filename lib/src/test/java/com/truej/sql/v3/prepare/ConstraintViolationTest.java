package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import com.truej.sql.v3.Constraint;
import com.truej.sql.v3.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ConstraintViolationTest {
    static class Handled extends Exception { }

    @Test void unhandled() throws SQLException {
        Fixture.withDataSource(ds ->
            assertThrows(ConstraintViolationException.class, () ->
                Fixture.queryStmt("insert into t1 values(1, 'x')").fetchNone(ds)
            )
        );
    }

    @Test void unhandledButTried() throws SQLException {
        Fixture.withDataSource(ds ->
            assertThrows(
                ConstraintViolationException.class, () -> {
                    try {
                        Fixture.queryStmt("insert into t1 values(1, 'x')").fetchNone(ds);
                    } catch (ConstraintViolationException ex) {
                        ex.when(new Constraint<>("t1", "t1_unknown", () -> {
                            throw new Handled();
                        }));
                    }
                }
            )
        );
    }

    @Test void rethrow() throws SQLException {
        Fixture.withDataSource(
            ds -> assertThrows(Handled.class, () -> {
                try {
                    Fixture.queryStmt("insert into t1 values(1, 'x')").fetchNone(ds);
                } catch (ConstraintViolationException ex) {
                    ex.when(
                        new Constraint<>("t1", "t1_pk", () -> {
                            throw new Handled();
                        })
                    );
                }
            })
        );
    }

    @Test void asValue() throws SQLException {
        Fixture.withDataSource(
            ds -> assertEquals(
                ((Supplier<Boolean>) () -> {
                    try {
                        Fixture.queryStmt("insert into t1 values(1, 'x')").fetchNone(ds);
                        return true;
                    } catch (ConstraintViolationException ex) {
                        return ex.when(
                            new Constraint<>("t1", "t1_pk", () -> false)
                        );
                    }
                }).get(), false
            )
        );
    }
}
