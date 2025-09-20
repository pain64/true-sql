package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import net.truej.sql.test.__05__GenerateDtoG;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.GroupTest1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.util.Formatter.pretty;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "Group must have at least one non-aggregated field (key)")
@TrueSql public class __47__GroupWithoutKey {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
            with tbl(x) as (
               values (1)
            ) select t.x as "a." from tbl t
            """
        ).g.fetchOne(GroupTest1.class);
    }
}
