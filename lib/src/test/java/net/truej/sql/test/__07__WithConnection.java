package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.fetch.Parameters.NotNull;

// FIXME: migrate this test to HSQLDB
@ExtendWith(TrueSqlTests.class) @EnableOn(POSTGRESQL)
@TrueSql public class __07__WithConnection {

    @TestTemplate public void test(MainDataSource ds) {
        var expectedTimeZone = "America/New_York";
        Assertions.assertEquals(
            expectedTimeZone, ds.withConnection(cn -> {
                    cn.q("set time zone 'America/New_York'").fetchNone();

                    return cn.q("select current_setting('TIMEZONE')")
                        .fetchOne(NotNull, String.class);
                }
            )
        );
    }
}
