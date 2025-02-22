package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoG.GroupTest;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(HSQLDB)
@TrueSql public class __19__GroupingDefaultTypesBoxedNames  {
    @TestTemplate public void test(MainDataSource ds) {
            ds.q("""
                select
                    gg as       "name",
                    bool as     "A a.o",
                    byte as     "  a.B b.c1",
                    short as    "  a.  b.C c.d1",
                    intt as     "  a.  b.  c.D d.e1",
                    long as     "  a.  b.  c.  d.E e.f1",
                    flo as      "  a.  b.  c.  d.  e.F f.g1",
                    doub as     "  a.  b.  c.  d.  e.  f.G g.h1",
                    1    as     "  a.  b.  c.  d.  e.  f.  g.H h.j1",
                    1    as     "  a.  b.  c.  d.  e.  f.  g.  h.p"
                from grouped_dto
                """).g.fetchOneOrZero(GroupTest.class);

    }
}
