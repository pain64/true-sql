package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "For parameter 2 mode mismatch. Expected IN but has INOUT"
)
@TrueSql public class __22__InButHas {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("{ call digit_magic(?, ?, ?) }", 1L, 2L, 3L).asCall().fetchNone();
    }
}
