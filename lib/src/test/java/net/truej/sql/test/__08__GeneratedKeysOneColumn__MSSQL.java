package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests.class) @EnableOn(MSSQL)
@TrueSql public class __08__GeneratedKeysOneColumn__MSSQL {

    @TestTemplate public void test(MainConnection cn) throws SQLException {
        cn.w.setAutoCommit(false);
        try {
            Assertions.assertEquals(
                3L, cn.q("insert into users output inserted.id values(?, ?, ?)", 3L, "Boris", null)
                    .fetchOne(NotNull, Long.class)
            );
        } finally {
            cn.w.rollback();
            cn.w.setAutoCommit(true);
        }
    }
}
