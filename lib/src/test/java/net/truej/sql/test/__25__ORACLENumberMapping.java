package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.fetch.Parameters;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.ORACLE;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(ORACLE)
@TrueSql public class __25__ORACLENumberMapping {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select 99 from dual").fetchOne(byte.class);
        ds.q("select 99 from dual").fetchOne(NotNull, Byte.class);
        ds.q("select 99 from dual").fetchOne(short.class);
        ds.q("select 99 from dual").fetchOne(NotNull, Short.class);
        ds.q("select 99 from dual").fetchOne(int.class);
        ds.q("select 99 from dual").fetchOne(NotNull, Integer.class);
        ds.q("select 99 from dual").fetchOne(long.class);
        ds.q("select 99 from dual").fetchOne(NotNull, Long.class);
    }
}
