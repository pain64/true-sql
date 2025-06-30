package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoG.SC;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(POSTGRESQL)
@TrueSql public class __26__SnakeCaseToCamelCase {
    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            "SC[helloItsMe=1]",
            ((Object) ds.q("select 1 as \"hello_its_me\"").g.fetchOne(SC.class)).toString()
        );
    }
}
