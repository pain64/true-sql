package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.ORACLE;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(ORACLE)
@TrueSql public class __02__UpdateCount_ORACLE {

    @TestTemplate public void test(MainConnection cn) throws SQLException {
        cn.w.setAutoCommit(false);

        try {
            Assertions.assertEquals(
                1L,
                cn.q("""
                    update bill
                    set discount = amount * ?
                    where trunc(bill."date") = to_date('01.09.2024', 'DD.MM.YYYY')
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
                        update bill
                        set discount = ?
                        where trunc(bill."date") = ?""",
                    v -> new Object[]{v.discount, v.date}
                ).withUpdateCount.fetchNone()
            );
        } finally {
            cn.w.rollback();
            cn.w.setAutoCommit(true);
        }
    }
}
