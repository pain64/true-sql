package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.source.Parameters.*;

@ExtendWith(TrueSqlTests2.class) @DisabledOn(POSTGRESQL)
@TrueSql public class __03__Call {
    // fixme: make int
    // todo: подумать над nullability для out параметров???
    record IntPair(
        @NotNull Integer first,
        @NotNull Integer second
    ) { }

    @TestTemplate public void test(MainConnection cn) {
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
            cn.q(
                List.of(
                    LocalDateTime.of(2024, 7, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 8, 1, 0, 0, 0)
                ),
                "{call discount_bill(?)}",
                d -> new Object[]{d}
            ).asCall().fetchNone()
        );
    }

}
