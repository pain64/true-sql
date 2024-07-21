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
    kind = ERROR, text = "Batch parameter extractor must be lambda literal returning " +
                         "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
)
@TrueSql public class __11__Batch_Lambda_ObjectArray_BadType {

    @TestTemplate public void test(MainConnection cn) {
        cn.q(List.of(""), "select 1", s -> new String[]{"xxx"}).fetchNone();
    }
}
