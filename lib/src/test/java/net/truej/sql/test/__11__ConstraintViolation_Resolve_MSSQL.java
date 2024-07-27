package net.truej.sql.test;

import net.truej.sql.Constraint;
import net.truej.sql.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.compiler.TrueSqlTests2.DisabledOn;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests2.class) @EnableOn(MSSQL)
@TrueSql public class __11__ConstraintViolation_Resolve_MSSQL {
    static class Handled extends Exception { }

    @TestTemplate public void implicit(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "clinic", "clinic_fk2", () -> { throw new Handled(); })
                );
            }
        });
    }

    @TestTemplate public void schema(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "dbo", "clinic", "clinic_fk2", () -> { throw new Handled(); })
                );
            }
        });
    }

    @TestTemplate public void catalog(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "master", "dbo", "clinic", "clinic_fk2", () -> { throw new Handled(); })
                );
            }
        });
    }
}
