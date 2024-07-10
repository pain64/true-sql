package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.PgConnection;
import net.truej.sql.compiler.PgDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.source.Parameters.*;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;

import static net.truej.sql.source.Parameters.unfold;
import static net.truej.sql.source.Parameters.unfold2;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __10__Unfold {
    @Test public void test(PgDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of("Joe", "Donald"),
            ds.q("select name from users where id in (?)", unfold(ids))
                .fetchList(String.class)
        );
        var data = List.of(new Pair<Long, BigDecimal>(1L, null), new Pair<Long, BigDecimal>(2L, null));

        Assertions.assertEquals(
            List.of(new BigDecimal("2000.55"), new BigDecimal("1000.20")),
            ds.q("select amount from bill where (id, discount) in = (?)", unfold2(data))
                .fetchList(BigDecimal.class)
        );
    }
}
