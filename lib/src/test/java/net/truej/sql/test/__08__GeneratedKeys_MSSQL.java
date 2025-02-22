package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests.class) @EnableOn(MSSQL)
@TrueSql public class __08__GeneratedKeys_MSSQL {

    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            3L, ds.q("insert into users(name, info) output inserted.id values(?, ?)", "Boris", null)
                .fetchOne(NotNull, Long.class)
        );
    }
}
