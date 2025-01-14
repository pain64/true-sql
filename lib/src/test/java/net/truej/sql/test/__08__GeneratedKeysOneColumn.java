package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.DisabledOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.fetch.Parameters.NotNull;

// FIXME: disable this check in compiler for HSQLDB
@ExtendWith(TrueSqlTests.class) @DisabledOn({HSQLDB, MSSQL})
@TrueSql public class __08__GeneratedKeysOneColumn {

    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            3L, ds.q("insert into users(name, info) values(?, ?)", "Boris", null)
                .asGeneratedKeys("id").fetchOne(NotNull, Long.class)
        );
    }
}
