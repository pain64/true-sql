package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static net.truej.sql.source.Parameters.*;

@ExtendWith(TrueSqlTests.class)

@TrueSql public class __03__Call {
    record IntPair (Integer first, Integer second) {}
    @Test public void test(MainConnection cn) {
        Assertions.assertEquals(
                new IntPair(20, 30),
                cn.q("{call digit_magic(?, ?, ?)}", 10, inout(10), out(Integer.class))
                        .asCall().fetchOne(IntPair.class)
        );
        //TODO: test on mssql
//        Assertions.assertEquals(
//                5L,
//                cn.q("{call bill_zero()}").asCall().withUpdateCount.fetchNone()
//        );

        Assertions.assertNull(
                cn.q(List.of(Timestamp.valueOf("2024-07-01 00:00:00"), Timestamp.valueOf("2024-08-01 00:00:00")),
                        "{call discount_bill(?)}",
                        d -> new Object[] {d}).asCall().fetchNone()
        );
    }

}
