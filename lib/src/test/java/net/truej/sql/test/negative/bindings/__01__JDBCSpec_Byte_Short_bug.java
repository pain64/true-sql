package net.truej.sql.test.negative.bindings;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;
import static net.truej.sql.source.Parameters.NotNull;

@ExtendWith(TrueSqlTests2.class)
@Message(
    kind = ERROR, text = "type mismatch for column 1 (for field `result`)." +
                         " Expected java.lang.Integer but has java.lang.Byte"
)
@Message(
    kind = ERROR, text = "type mismatch for column 1 (for field `result`)." +
                         " Expected int but has java.lang.Byte"
)
@Message(
    kind = ERROR, text = "type mismatch for column 1 (for field `result`)." +
                         " Expected java.lang.Integer but has java.lang.Short"
)
@Message(
    kind = ERROR, text = "type mismatch for column 1 (for field `result`)." +
                         " Expected int but has java.lang.Short"
)
@TrueSql public class __01__JDBCSpec_Byte_Short_bug {
    @TestTemplate @DisabledOn(POSTGRESQL) public void test(MainConnection cn) {
        cn.q("values cast (1 as tinyint)").fetchOne(NotNull, Integer.class);
        cn.q("values cast (1 as tinyint)").fetchOne(NotNull, int.class);
        cn.q("values cast (1 as smallint)").fetchOne(NotNull, Integer.class);
        cn.q("values cast (1 as smallint)").fetchOne(NotNull, int.class);
    }
}
