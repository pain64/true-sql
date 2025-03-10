package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.compiler.TrueSqlTests.DisabledOn;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __02__UpdateCountToString {

    @TestTemplate public void test(MainConnection cn) {
        var result = cn.q(
            "insert into users values(default, ?, ?)",
            "Mike", "Strong left hook"
        ).asGeneratedKeys("id").withUpdateCount.fetchOne(long.class);

        Assertions.assertEquals("UpdateResult[updateCount=1, value=3]", result.toString());
    }
}
