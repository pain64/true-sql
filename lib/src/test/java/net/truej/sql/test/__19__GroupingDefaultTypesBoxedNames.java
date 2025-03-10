package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoG.GroupTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __19__GroupingDefaultTypesBoxedNames {
    @TestTemplate public void test(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [{"name":"","specialSnakeCase":1,"a":[{"o":true,"b":[{"c1":1,"c":[{"d1":2,"d":[{"e1":3,"e":[{"f1":4,"f":[{"g1":5.0,"g":[{"h1":6.0,"h":[{"j1":1,"p":1}]}]}]}]}]}]}]}]}]""",
            new ObjectMapper()
                .writeValueAsString(
                    ds.q("""
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
                        from grouped_dto
                        """).g.fetchList(GroupTest.class)
                )
        );
    }
}
