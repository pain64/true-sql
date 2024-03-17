package com.truej.sql.showcase;

import com.truej.sql.v3.Constraint;
import com.truej.sql.v3.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.function.Supplier;

import static com.truej.sql.v3.TrueSql.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class __04__ConstraintViolation {
    static class Handled extends Exception { }

    @Test void unhandled(DataSource ds) {
        assertThrows(ConstraintViolationException.class, () ->
            Stmt."insert into users values(1, 'John', 'xxx@email.com')".fetchNone(ds)
        );
    }

    @Test void rethrow(DataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                Stmt."insert into users values(1, 'John', 'xxx@email.com')".fetchNone(ds);
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>("users", "users_pk", () -> {
                        throw new Handled();
                    })
                );
            }
        });
    }

    @Test void asValue(DataSource ds) {
        assertEquals(
            ((Supplier<Boolean>) () -> {
                try {
                    Stmt."insert into users values(1, 'John', 'xxx@email.com')".fetchNone(ds);
                    return true;
                } catch (ConstraintViolationException ex) {
                    return ex.when(
                        new Constraint<>("users", "users_pk", () -> false)
                    );
                }
            }).get(), false
        );
    }
}
