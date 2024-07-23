package net.truej.sql.test;

import net.truej.sql.Constraint;
import net.truej.sql.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __11__ConstraintViolation_Resolve_HSQLDB {
    static class Handled extends Exception { }

    @EnableOn(Database.HSQLDB) @TestTemplate public void schemaAndCatalog(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    new Constraint<>(ds, "public", "public", "users", "users_pk", () -> { throw new Handled(); })
                );
            }
        });
    }
}
