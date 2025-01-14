package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "Source identifier may be method parameter, class field or " +
                         "local variable initialized by new (var ds = new MySourceW(...)). " +
                         "Type of source identifier cannot be generic parameter"
)
@EnableOn(HSQLDB) @TrueSql public class __02__BadSource_Local {
    @TestTemplate public void test(MainConnection cn) {
        var cn2 = cn;
        cn2.q("select 1").fetchNone();
    }
}
