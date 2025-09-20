package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.test.__05__GenerateDtoG.GroupTest3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.util.Formatter.pretty;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __29__EmptyGroupMustNotSkipNonEmpty {
    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            """
              GroupTest3[
                k=1, a=[
                  ]
                , b=[
                  1]
                ]
              """,
            pretty(
                ds.q("""
                    with tbl(x, y, z) as (values (1, cast(null as int), 1))
                    select
                        t.x as "k" ,
                        t.y as "a.",
                        t.z as "b."
                    from tbl t"""
                ).g.fetchOne(GroupTest3.class)
            )
        );
    }
}
