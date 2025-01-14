package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "expected Name.class or full.qualified.Name.class or array[].class"
) @EnableOn(HSQLDB)
@TrueSql public class __13__FetchTo_IsNotClassLiteral_FQN {
    record ToDto() {}

    @TestTemplate public void test(MainConnection cn) {
        cn.q("select id from users").fetchOne(__13__FetchTo_IsNotClassLiteral_FQN.ToDto.class);
    }
}
