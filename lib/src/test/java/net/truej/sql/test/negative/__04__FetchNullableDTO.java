package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.fetch.Parameters.Nullable;



@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.Message(
    kind = ERROR, text = "Nullable or NotNull hint not allowed for aggregated DTO"
)
@TrueSql public class __04__FetchNullableDTO {
    record User(Long id, String name, @Nullable String info) {}
    @TestTemplate public void test(MainConnection cn) {

        cn.q("select * from users").fetchList(Nullable, User.class);
    }
}
