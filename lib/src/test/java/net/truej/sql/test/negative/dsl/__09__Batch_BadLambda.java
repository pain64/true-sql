package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "Batch parameter extractor must be lambda literal returning " +
                         "object array literal (e.g. `b -> new Object[]{b.f1, b.f2}`)"
)
@EnableOn(HSQLDB) @TrueSql public class __09__Batch_BadLambda {

    @TestTemplate public void test(MainConnection cn) {
        cn.q(List.of(""), "select 1", null).fetchNone();
    }
}
