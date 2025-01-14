package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.fetch.TooFewRowsException;
import net.truej.sql.fetch.TooMuchRowsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __03__WrongFetch {
    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertThrows(
            TooMuchRowsException.class,
            () -> ds.q("select id from users").fetchOne(Long.class)
        );

        Assertions.assertThrows(
            TooFewRowsException.class,
            () -> ds.q("select id from users where id = 99").fetchOne(Long.class)
        );
    }
}
