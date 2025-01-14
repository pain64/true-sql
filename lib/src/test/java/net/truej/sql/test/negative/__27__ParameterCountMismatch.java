package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.compiler.TrueSqlTests.Message;


@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "parameter count mismatch. expected 3 but has 2"
)
@TrueSql public class __27__ParameterCountMismatch {
    record AB(int a, int b) { }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("values ?, ?, ?", 1, 2)
            .asCall().fetchOne(AB.class);
    }
}
