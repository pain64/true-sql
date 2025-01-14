package net.truej.sql.test;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(POSTGRESQL)
@TrueSql public class __22__InParameterAsFunctionCall {
    long getUserId() { return 1L; }

    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            "Joe",
            ds.q(
                "select name from users where id = ?", getUserId()
            ).fetchOne(String.class)
        );
    }
}
