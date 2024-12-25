package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;


@ExtendWith(TrueSqlTests2.class) @EnableOn(POSTGRESQL)
@TrueSql public class __01__Fetch_ByteArray__POSTGRESQL {

    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertArrayEquals(
            new byte[]{1, 2, 3},
            cn.q("select decode('010203', 'hex')").fetchOne(byte[].class)
        );
    }
}
