package net.truej.sql.test;

import net.truej.sql.Constraint;
import net.truej.sql.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Supplier;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO: report bug to mysql and mariadb drivers. table name, schema name not avail in exception
@ExtendWith(TrueSqlTests2.class) @DisabledOn({MYSQL, MARIADB})
@TrueSql public class __11__ConstraintViolation {
    static class Handled extends Exception { }

    @TestTemplate  public void unhandled(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () ->
            ds.q("delete from city").fetchNone()
        );
    }

    @TestTemplate public void rethrow(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "clinic", "clinic_fk2", () -> {
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
                    ds.q("delete from city").fetchNone();
                    return true;
                } catch (ConstraintViolationException ex) {
                    return ex.when(
                        new Constraint<>(ds, "clinic", "clinic_fk2", () -> false)
                    );
                }
            }).get(), false
        );
    }

    @TestTemplate public void notCatch(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(

                );
            }
        });
    }
}
