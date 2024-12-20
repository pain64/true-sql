package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.fetch.Parameters.*;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __10__Unfold {

    @TestTemplate public void unfold1(MainDataSource ds) {
        var ids = List.of(1L, 2L, 3L);

        Assertions.assertEquals(
            List.of("Joe", "Donald"),
            ds.q("select name from users where id in (?)", unfold(ids))
                .fetchList(String.class)
        );
    }

    @TestTemplate public void unfold1AsObjectArray(MainDataSource ds) {
        var ids = List.of(1L, 2L, 3L);

        Assertions.assertEquals(
            List.of("Joe", "Donald"),
            ds.q(
                "select name from users where id in (?)",
                unfold(ids, i -> new Object[]{i})
            ).fetchList(String.class)
        );
    }
}
