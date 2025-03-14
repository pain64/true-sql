package net.truej.sql.test.negative;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "has more than one non-empty args constructor"
)
@EnableOn(HSQLDB) @TrueSql public class __06__TooMuchNonEmptyConstructors {
    static class User {
        Long id;
        String name;
        @Nullable String info;
        public User(Long id) {
            this.id = id;
        }
        public User(String name) {
            this.name = name;
        }
    }
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select * from users").fetchList(User.class);
    }
}
