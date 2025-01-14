package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.compiler.TrueSqlTests.Message;
import static net.truej.sql.fetch.Parameters.*;


@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "result of primitive type cannot be marked as Nullable"
)
@Message(
    kind = ERROR, text = "result of primitive type not needed to be marked as NotNull"
)
@TrueSql public class __26__ResultPrimitiveTypeNullability {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("values 1").fetchOne(Nullable, int.class);
        cn.q("values 1").fetchOne(NotNull, int.class);
    }
}
