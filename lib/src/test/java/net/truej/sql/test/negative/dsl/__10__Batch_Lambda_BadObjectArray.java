package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "Batch parameter extractor must be lambda literal returning " +
                         "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
)
@EnableOn(HSQLDB) @TrueSql public class __10__Batch_Lambda_BadObjectArray {

    @TestTemplate public void test(MainConnection cn) {
        var anArray = new Object[]{"xxx"};
        cn.q(List.of(""), "select 1", s -> anArray).fetchNone();
    }
}
