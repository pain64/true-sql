package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.source.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __02__UpdateCount {
    @Test
    public void test(MainConnection cn) {
        Assertions.assertEquals(
                1L,
                cn.q("""
                        update bill
                        set discount = amount * ?
                        where cast(date as date) = '2024-09-01'
                        """, new BigDecimal("0.1")
                ).withUpdateCount.fetchNone()
        );

        record DateDiscount (Timestamp date, BigDecimal discount) {}

        var discounts = List.of(
                new DateDiscount(Timestamp.valueOf("2024-07-01 00:00:00"), new BigDecimal("0.2")),
                new DateDiscount(Timestamp.valueOf("2024-08-01 00:00:00"), new BigDecimal("0.15"))
        );

        Assertions.assertArrayEquals(
                new long[] {2L, 2L},
                cn.q(discounts,
                    """
                            update bill
                            set discount = ?
                            where cast(date as date) = cast(? as date)""",
                    v -> new Object[]{v.discount, v.date}).withUpdateCount.fetchNone()
        );

        //TODO: ADD test with generated keys to fetch someone
    }
}
