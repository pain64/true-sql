package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __02__UpdateCount {

    @Test
    public void test(MainConnection cn) {
        Assertions.assertEquals(
                1L,
                cn.q("update bill set discount = amount * 0.1").withUpdateCount.fetchNone()
        );
        
    }
}
