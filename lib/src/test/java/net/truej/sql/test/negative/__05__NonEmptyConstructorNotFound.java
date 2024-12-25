package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.Message(
    kind = ERROR, text = "non-empty args constructor not found"
)
@TrueSql public class __05__NonEmptyConstructorNotFound {
    static class User {
        Long id;
        String name;
        @Nullable String info;
    }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("select * from users").fetchList(User.class);
    }
}
