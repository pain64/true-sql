package net.truej.sql.test;

import net.truej.sql.fetch.ConstraintViolationException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Enjoy https://bugs.mysql.com/bug.php?id=1956

// FIXME: MariaDB - заставить дебилов сделать нормальное исключение для ConstraintViolation
@ExtendWith(TrueSqlTests2.class) @EnableOn({MYSQL})
@TrueSql public class __11__ConstraintViolation_Resolve_MYSQL_MARIADB {
    static class Handled extends Exception { }

    @TestTemplate public void implicit(MainDataSource ds) {
        assertThrows(Handled.class, () -> {
            try {
                ds.q("insert into users values(1, 'Joe', null)").fetchNone();
            } catch (ConstraintViolationException ex) {
                ex.when(
                    ds.constraint("users", "primary", () -> { throw new Handled(); })
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
                    ds.constraint("test", "users", "primary", () -> { throw new Handled(); })
                );
            }
        });
    }
}
