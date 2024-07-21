package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "Expected identifier for source for `.q`"
)
@TrueSql public class __03__BadSource_NotIdentifier {
    record Bar(MainConnection f) {}

    @TestTemplate public void test(MainConnection cn) {
        var bar = new Bar(cn);
        bar.f.q("select 1");
    }
}
