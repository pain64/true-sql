package net.truej.sql.test;

import net.truej.sql.Constraint;
import net.truej.sql.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.Main;

import java.util.function.Supplier;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __11__ConstraintViolation {
    static class Handled extends Exception { }

    @TestTemplate  public void unhandled(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () ->
            ds.q("insert into users values(1, 'Joe', null)").fetchNone()
        );
    }

    @TestTemplate public void rethrow(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "users", "users_pk", () -> {
                        throw new Handled();
                    })
                );
            }
        });
    }

    @TestTemplate public void asValue(MainDataSource ds) {
        Assertions.assertEquals(
            ((Supplier<Boolean>) () -> {
                try {
                    ds.q("insert into users values(1, 'Joe', null)").fetchNone();
                    return true;
                } catch (ConstraintViolationException ex) {
                    return ex.when(
                        new Constraint<>(ds, "users", "users_pk", () -> false)
                    );
                }
            }).get(), false
        );
    }
}
