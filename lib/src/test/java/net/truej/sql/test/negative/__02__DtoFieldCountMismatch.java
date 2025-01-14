package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.Message(
    kind = ERROR, text = "target type implies 1 columns but result has 3"
)
@TrueSql public class __02__DtoFieldCountMismatch {

    @TestTemplate public void test(MainConnection cn) {
        cn.q("select id, name, info from users").fetchOne(String.class);
    }
}
