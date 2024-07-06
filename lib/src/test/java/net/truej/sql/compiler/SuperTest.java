package net.truej.sql.compiler;

import net.truej.sql.TrueSql;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


// FIXME: does not works if this record will be in class or method

@ExtendWith(TrueSqlTests.class)
@TrueSql public class SuperTest {
    @Disabled
    @Test public void test1(MainConnection cn) {
        cn.q("insert into t1 values(100, ?)", "haha")
            .asGeneratedKeys("id").fetchOne(Long.class);
    }
}
