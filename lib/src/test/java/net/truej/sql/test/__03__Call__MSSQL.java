package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.compiler.TrueSqlTests.DisabledOn;
import static net.truej.sql.fetch.Parameters.inout;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests.class) @EnableOn(MSSQL)
@TrueSql public class __03__Call__MSSQL {
    record IntPair(int first, int second) { }

    @TestTemplate public void test(MainConnection cn) {
        // batch call
        Assertions.assertNull(
            cn.q(
                List.of(
                    LocalDateTime.of(2024, 7, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 8, 1, 0, 0, 0)
                ),
                "{call discount_bill(?)}",
                d -> new Object[]{d}
            ).asCall().fetchNone()
        );
    }

}
