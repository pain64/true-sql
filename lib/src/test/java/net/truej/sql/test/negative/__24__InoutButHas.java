package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.inout;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "For parameter 1 mode mismatch. Expected INOUT but has IN"
)
@TrueSql public class __24__InoutButHas {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("{ call digit_magic(?, ?, ?) }", inout(2), out(Integer.class), 3L).asCall().fetchNone();
    }
}
