package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "Query text must be a string literal"
)
@TrueSql public class __08__QueryNotStringLiteral_Batch {

    @TestTemplate public void test(MainConnection cn) {
        cn.q(List.of(""), "a" + 1, s -> new Object[]{s}).fetchNone();
    }
}
