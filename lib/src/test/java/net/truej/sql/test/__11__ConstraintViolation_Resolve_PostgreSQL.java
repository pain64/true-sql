package net.truej.sql.test;

import net.truej.sql.dsl.Constraint;
import net.truej.sql.dsl.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TrueSqlTests2.class) @EnableOn(Database.POSTGRESQL)
@TrueSql public class __11__ConstraintViolation_Resolve_PostgreSQL {


    static class Handled extends Exception { }

    @TestTemplate public void schemaAndCatalog(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("test", "public", "users", "users_pk", () -> {
                        throw new Handled();
                    })
                );
            }
        });
    }
}
