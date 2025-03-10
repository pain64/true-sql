package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import net.truej.sql.source.DataSourceW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "Expected identifier for source for `.constraint(...)`")
@TrueSql public class __44__ConstraintUnresolvedSource {
    static DataSourceW f(DataSourceW ds) {
        return ds;
    }
    @TestTemplate public void test(MainDataSource ds) {
        f(ds).constraint("", "", null);
    }
}
