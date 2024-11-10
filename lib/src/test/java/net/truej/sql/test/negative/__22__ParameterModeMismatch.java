package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "for parameter 2 expected mode IN but has INOUT"
)
@TrueSql public class __22__ParameterModeMismatch {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("{ call digit_magic(?, ?, ?) }", 1, 2, 3).asCall().fetchNone();
    }
}
