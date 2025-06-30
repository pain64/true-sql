package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoG.GroupTest;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __19__GroupingDefaultTypesBoxedNames {
    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            "[GroupTest[name=, specialSnakeCase=1, a=[A[o=true, b=[B[c1=1, c=[C[d1=2, d=[D[e1=3, e=[E[f1=4, f=[F[g1=5.0, g=[G[h1=6.0, h=[H[j1=1, p=1]]]]]]]]]]]]]]]]]]",
            ((Object) ds.q("""
                select
                    gg   as     "name",
                    1    as     "SPECIAL_SNAKE_CASE",
                    bool as     "A a.o",
                    byte as     "  a.B b.c1",
                    short as    "  a.  b.C c.d1",
                    intt as     "  a.  b.  c.D d.e1",
                    long as     "  a.  b.  c.  d.E e.f1",
                    flo as      "  a.  b.  c.  d.  e.F f.g1",
                    doub as     "  a.  b.  c.  d.  e.  f.G g.h1",
                    1    as     "  a.  b.  c.  d.  e.  f.  g.H h.j1",
                    1    as     "  a.  b.  c.  d.  e.  f.  g.  h.p"
                from grouped_dto"""
            ).g.fetchList(GroupTest.class)).toString()
        );
    }
}
