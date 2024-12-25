package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoTrueSql.GroupTest2;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSql public class __19__GroupingDefaultTTypesBoxedNames2 {
    @TestTemplate
    public void test(MainDataSource ds) {
        ds.q("""
                select
                    gg as      "name",
                    flo as     "A a.o",
                    1 as       "  a.P b.r",
                    1 as       "  a.  b.k"
                from grouped_dto
                """).g.fetchOneOrZero(GroupTest2.class);
    }
}