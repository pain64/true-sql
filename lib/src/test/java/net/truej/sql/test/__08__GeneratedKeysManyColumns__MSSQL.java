package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class) @EnableOn(MSSQL)
@TrueSql public class __08__GeneratedKeysManyColumns__MSSQL {

    record IdAndName(long id, String name) { }

    @TestTemplate public void test(MainConnection cn) throws SQLException {
        cn.w.setAutoCommit(false);

        try {
            Assertions.assertEquals(
                new IdAndName(3L, "Boris"),
                cn.q(
                    "insert into users output inserted.id, inserted.name values(?, ?, ?)",
                    3L, "Boris", null
                ).fetchOne(IdAndName.class)
            );
        } finally {
            cn.w.rollback();
            cn.w.setAutoCommit(true);
        }
    }
}
