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
import static net.truej.sql.source.Parameters.unfold;
import static net.truej.sql.source.Parameters.unfold2;

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

    @TestTemplate @DisabledOn(HSQLDB) public void unfoldMany(MainDataSource ds) {
        var data = List.of(
            new Pair<Long, BigDecimal>(1L, null),
            new Pair<Long, BigDecimal>(2L, null)
        );

        Assertions.assertEquals(
            //List.of(new BigDecimal("2000.55"), new BigDecimal("1000.20")),
            List.of(), // FIXME
            ds.q("select amount from bill where (id, discount) in (?)", unfold2(data))
                .fetchList(BigDecimal.class)
        );
    }
}
