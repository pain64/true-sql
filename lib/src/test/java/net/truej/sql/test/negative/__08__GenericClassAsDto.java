package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.*;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "To Dto class cannot be generic"
)
@TrueSql public class __08__GenericClassAsDto {
    static class User<T> {
        public User(T t) { }
    }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(User.class);
    }
}
