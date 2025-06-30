package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.ORACLE;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(ORACLE)
@TrueSql public class __25__ORACLENumberMapping {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select 99 from dual").fetchOne(byte.class);
        cn.q("select 99 from dual").fetchOne(NotNull, Byte.class);
        cn.q("select 99 from dual").fetchOne(short.class);
        cn.q("select 99 from dual").fetchOne(NotNull, Short.class);
        cn.q("select 99 from dual").fetchOne(int.class);
        cn.q("select 99 from dual").fetchOne(NotNull, Integer.class);
        cn.q("select 99 from dual").fetchOne(long.class);
        cn.q("select 99 from dual").fetchOne(NotNull, Long.class);
    }
}
