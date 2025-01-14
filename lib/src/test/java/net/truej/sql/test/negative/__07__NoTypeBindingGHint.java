package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto6;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests.class)
@Message(
    kind = ERROR, text = "has no binding for type XXX"
) @EnableOn(POSTGRESQL)
@TrueSql public class __07__NoTypeBindingGHint {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select 1 as \":t XXX\"").g.fetchList(Dto6.class);
    }
}
