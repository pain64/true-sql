package com.truej.sql.showcase;

import com.truej.sql.v3.Constraint;
import com.truej.sql.v3.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class __04__ConstraintViolation {
    static class Handled extends Exception { }

    void unhandled(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () ->
            ds."insert into users values(1, 'John', 'xxx@email.com')".fetchNone()
        );
    }

    void rethrow(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds."insert into users values(1, 'John', 'xxx@email.com')".fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>("users", "users_pk", () -> {
                        throw new Handled();
                    })
                );
            }
        });
    }

    void asValue(MainDataSource ds) {
        assertEquals(
            ((Supplier<Boolean>) () -> {
                try {
                    ds."insert into users values(1, 'John', 'xxx@email.com')".fetchNone();
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
