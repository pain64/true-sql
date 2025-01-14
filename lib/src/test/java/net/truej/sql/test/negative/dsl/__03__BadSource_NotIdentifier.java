package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "Expected identifier for source for `.q(...)`"
)
@EnableOn(HSQLDB) @TrueSql public class __03__BadSource_NotIdentifier {
    record Bar(MainConnection f) {}

    @TestTemplate public void test(MainConnection cn) {
        var bar = new Bar(cn);
        bar.f.q("select 1").fetchNone();
    }
}
