package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests2.class) @TrueSql public class __XXX__ {
    record User(Long id, String name, @Nullable String info) {}

    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertNull(
            cn.q("insert into users values(?, ?, ?)", 4L, "Ali", null).fetchNone()
        );

        Assertions.assertEquals(
            new User(4L, "Ali", null),
            cn.q(
                "select id, name, info from users where id = ?", 4L
            ).fetchOne(User.class)
        );
    }
}
