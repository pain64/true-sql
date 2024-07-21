package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@Disabled
@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "expected %SimpleName%.class as fetch result"
)
@TrueSql public class __13__FetchTo_IsNotClassLiteral_FQN {
    record ToDto() {}
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select id from users").fetchOne(__13__FetchTo_IsNotClassLiteral_FQN.ToDto.class);
    }
}
