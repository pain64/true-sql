package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __18__ScalarResultOfPrimitiveType {
    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            1L, ds.q("select id from users where id = 1").fetchOne(long.class)
        );
    }
}
