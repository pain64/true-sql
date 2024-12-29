package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(HSQLDB)
@TrueSqlTests2.Message(kind = ERROR, text = "nullability mismatch for column 1 (for field `result`). Your decision is " +
    "DEFAULT_NOT_NULL but driver infers EXACTLY_NULLABLE")
@TrueSql public class __35__NullabilityMismatch {
    @TestTemplate
    public void test(MainConnection cn) {
        cn.q("""
            values cast(null as int)
            """).fetchOne(Integer.class);
    }
}
