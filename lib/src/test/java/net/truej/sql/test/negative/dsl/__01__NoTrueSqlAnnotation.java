package net.truej.sql.test.negative.dsl;

import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "TrueSql DSL used but class not annotated with @TrueSql"
) @EnableOn(HSQLDB) public class __01__NoTrueSqlAnnotation {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select 1");
    }
}
