package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __02__UpdateCount {

    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertEquals(
            1L,
            cn.q("""
                update bill b
                set discount = amount * ?
                where cast(b.date as date) = '2024-09-01'
                """, new BigDecimal("0.1")
            ).withUpdateCount.fetchNone()
        );

        record DateDiscount(LocalDate date, BigDecimal discount) { }

        var discounts = List.of(
            new DateDiscount(LocalDate.of(2024, 7, 1), new BigDecimal("0.2")),
            new DateDiscount(LocalDate.of(2024, 8, 1), new BigDecimal("0.15"))
        );

        Assertions.assertArrayEquals(
            new long[]{2L, 2L},
            cn.q(
                discounts,
                """
                    update bill b
                    set discount = ?
                    where cast(b.date as date) = ?""",
                v -> new Object[]{v.discount, v.date}
            ).withUpdateCount.fetchNone()
        );

        //TODO: ADD test with generated keys to fetch someone
    }
}
