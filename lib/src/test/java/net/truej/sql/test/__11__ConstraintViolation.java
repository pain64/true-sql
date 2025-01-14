package net.truej.sql.test;

import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.fetch.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Supplier;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO: report bug to mysql and mariadb drivers. table name, schema name not avail in exception
@ExtendWith(TrueSqlTests.class) @DisabledOn({MYSQL, MARIADB})
@TrueSql public class __11__ConstraintViolation {
    static class Handled extends Exception { }

    @TestTemplate  public void unhandled(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () ->
            ds.q("delete from city").fetchNone()
        );
    }

    @TestTemplate  public void triedButUnhandled(MainDataSource ds) {
        assertThrows(ConstraintViolationException.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("users", "users_pk", () -> {
                        throw new Handled();
                    })
                );
            }
        });
    }

    @TestTemplate  public void uncheckedDS(MainDataSourceUnchecked ds) {
        ds.constraint("not_exists", "not_exists_constraint", () -> 42);
    }

    @TestTemplate public void rethrow(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("clinic", "clinic_fk2", () -> {
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
                        // is it better ?
                        // ds.constraint("clinic", "clinic_fk2", () -> false),
                        ds.constraint("clinic", "clinic_fk2", () -> false)
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
