package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.PgDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@ExtendWith(TrueSqlTests.class)
@TrueSql
public class __08__GeneratedKeys {
    record Discount(Long id, @Nullable BigDecimal discount) {
    }

    @Test
    public void test(PgDataSource ds) {

        Assertions.assertEquals(
                3L,
                ds.q("insert into users values(?, ?, ?)", 3L, "Boris", null)
                        .asGeneratedKeys("id").fetchOne(Long.class)
        );

        Assertions.assertEquals(
                List.of(new Discount(1L, new BigDecimal("400.11")),
                        new Discount(2L, new BigDecimal("200.04")),
                        new Discount(3L, new BigDecimal("1000.00")),
                        new Discount(4L, new BigDecimal("1400.15")),
                        new Discount(5L, new BigDecimal("100.02"))),
                ds.q("update bill set discount = amount * ?", new BigDecimal("0.2"))
                        .asGeneratedKeys("id", "discount").fetchList(Discount.class)
        );
        var expected1 = new UpdateResult<>(5L,
            List.of(new Discount(1L, new BigDecimal("400.11")),
                new Discount(2L, new BigDecimal("200.04")),
                new Discount(3L, new BigDecimal("1000.00")),
                new Discount(4L, new BigDecimal("1400.15")),
                new Discount(5L, new BigDecimal("100.02"))));
        var actual1 = ds.q("update bill set discount = amount * ?", new BigDecimal("0.2"))
            .asGeneratedKeys("id", "discount").withUpdateCount.fetchList(Discount.class);


//        record DateDiscount(Timestamp date, BigDecimal discount) {
//        }
//
//        var discounts = List.of(
//                new DateDiscount(Timestamp.valueOf("2024-07-01 00:00:00"), new BigDecimal("0.2")),
//                new DateDiscount(Timestamp.valueOf("2024-08-01 00:00:00"), new BigDecimal("0.15"))
//        );
//
//        Assertions.assertEquals(
//                List.of(new Discount(1L, new BigDecimal("20.00")),
//                        new Discount(2L, new BigDecimal("20.00")),
//                        new Discount(3L, new BigDecimal("15.00")),
//                        new Discount(4L, new BigDecimal("15.00"))),
//                ds.q(discounts,
//                                """
//                                        update bill
//                                        set discount = 100 * ?
//                                        where date::date = ?::date""",
//                                v -> new Object[]{v.discount, v.date}).asGeneratedKeys("id", "discount")
//                        .fetchList(Discount.class)
//        );

//        Assertions.assertEquals(
//                new UpdateResult<>(new long[]{2L, 2L},
//                        List.of(new Discount(1L, new BigDecimal("20.00")),
//                                new Discount(2L, new BigDecimal("20.00")),
//                                new Discount(3L, new BigDecimal("15.00")),
//                                new Discount(4L, new BigDecimal("15.00")))
//                ),
//
//                ds.q(discounts,
//                                """
//                                        update bill
//                                        set discount = 100 * ?
//                                        where date::date = ?::date""",
//                                v -> new Object[]{v.discount, v.date}).asGeneratedKeys("id", "discount")
//                        .withUpdateCount.fetchList(Discount.class)
//        );
    }
}
