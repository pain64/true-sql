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
@TrueSql public class __12__FetchTo_IsNotClassLiteral_Identifier {

    @TestTemplate public void test(MainConnection cn) {
        var stringClass = String.class;
        cn.q("select id from users").fetchOne(stringClass);
    }
}
