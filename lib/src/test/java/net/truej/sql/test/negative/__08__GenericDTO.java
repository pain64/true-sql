package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@Disabled
@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "has no type binding for net.truej.sql.test.negative.__07__NoTypeBinding_.User.Trap"
)
@TrueSql public class __08__GenericDTO {
     class User<T> {
        Long id;
        String name;
        @Nullable String info;
        T t;

        public User(Long id, String name, @Nullable String info) {
            this.id = id;
            this.name = name;
            this.info = info;
        }
        public T t() {
            return t;
        }
    }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(User.class);
    }
}
