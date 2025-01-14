package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.Message;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.WARNING;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = WARNING, text =
    "nullability mismatch for column 3 (for field `info`). Your decision is EXACTLY_NOT_NULL but driver infers EXACTLY_NULLABLE"
)
@TrueSql public class __01__NullabilityWarning {
    record User(Long id, String name, @NotNull String info) { }

    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertEquals(
            new User(2L, "Donald", "Do not disturb"),
            cn.q(
                "select id, name, info from users where id = ?", 2L
            ).fetchOne(User.class)
        );
    }
}
