package net.truej.sql.test;

import net.truej.sql.fetch.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests.class) @EnableOn(MSSQL)
@TrueSql public class __11__ConstraintViolation_Resolve_MSSQL {
    static class Handled extends Exception { }

    @TestTemplate public void implicit(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("delete from city").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("clinic", "clinic_fk2", () -> { throw new Handled(); })
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
                    ds.constraint("dbo", "clinic", "clinic_fk2", () -> { throw new Handled(); })
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
                    ds.constraint("master", "dbo", "clinic", "clinic_fk2", () -> { throw new Handled(); })
                );
            }
        });
    }
}
