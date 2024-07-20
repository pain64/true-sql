package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import net.truej.sql.source.Parameters.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.source.Parameters.*;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __10__Unfold {
    @TestTemplate public void unfold1(MainDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of("Joe", "Donald"),
            ds.q("select name from users where id in (?)", unfold(ids))
                .fetchList(String.class)
        );
    }
}
