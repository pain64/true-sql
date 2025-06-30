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

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __02__UpdateCountToString {

    @TestTemplate public void test(MainConnection cn) throws SQLException {
        cn.w.setAutoCommit(false);

        try {
            var result = cn.q(
                "insert into users values(3, ?, ?)",
                "Mike", "Strong left hook"
            ).asGeneratedKeys("id").withUpdateCount.fetchOne(long.class);

            Assertions.assertEquals("UpdateResult[updateCount=1, value=3]", result.toString());
        } finally {
            cn.w.rollback();
            cn.w.setAutoCommit(true);
        }
    }
}
