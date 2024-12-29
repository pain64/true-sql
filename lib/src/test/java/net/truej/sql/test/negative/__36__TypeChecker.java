package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import static net.truej.sql.fetch.Parameters.NotNull;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(HSQLDB)
@TrueSqlTests2.Message(kind = ERROR, text = "type mismatch for column 1 (for field `result`). Expected java.lang.String" +
    " but has java.lang.Integer")
@TrueSql public class __36__TypeChecker {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
            values 1
            """).fetchOne(NotNull, String.class);
    }
}
