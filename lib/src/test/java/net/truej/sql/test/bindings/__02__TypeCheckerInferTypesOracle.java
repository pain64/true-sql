package net.truej.sql.test.bindings;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.fetch.Parameters;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.bindings.__02__TypeCheckerInferTypesOracleG.*;
import static net.truej.sql.fetch.Parameters.NotNull;

import java.time.ZonedDateTime;

import static net.truej.sql.compiler.TrueSqlTests2.Database.ORACLE;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(ORACLE)
@TrueSql public class __02__TypeCheckerInferTypesOracle {
    @TestTemplate public void test(MainConnection cn) {
        var result = cn.q("""
            SELECT TO_TIMESTAMP_TZ('1999-12-01 11:00:00 -8:00', 'YYYY-MM-DD HH:MI:SS TZH:TZM') as ttz FROM DUAL
            """).g.fetchOne(XXX.class);

    }

    @TestTemplate public  void test2(MainConnection cn) {
        var result = cn.q("""
            SELECT TO_TIMESTAMP_TZ('1999-12-01 11:00:00 -8:00', 'YYYY-MM-DD HH:MI:SS TZH:TZM') as ttz FROM DUAL
            """).fetchOne(NotNull, ZonedDateTime.class);
    }
}
