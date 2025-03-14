package net.truej.sql.test;

import net.truej.sql.fetch.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(ORACLE)
@TrueSql public class __11__ConstraintViolation_Resolve_ORACLE {
    static class Handled extends Exception { }

    @TestTemplate public void implicit(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("users", "users_pk", () -> { throw new Handled(); })
                );
            }
        });
    }

    @TestTemplate public void schema(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("testuser", "users", "users_pk", () -> { throw new Handled(); })
                );
            }
        });
    }
}
