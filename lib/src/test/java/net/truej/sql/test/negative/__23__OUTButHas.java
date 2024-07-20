package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;
import static net.truej.sql.source.Parameters.out;

@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "For parameter 2 mode mismatch. Expected OUT but has INOUT"
)
@TrueSql public class __23__OUTButHas {
    @TestTemplate
    @TrueSqlTests2.DisabledOn(POSTGRESQL)
    public void test(MainConnection cn) {
        cn.q("{ call digit_magic(?, ?, ?) }", 1L, out(Integer.class), 3L).asCall().fetchNone();
    }
}
