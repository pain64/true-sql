package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.source.DataSourceW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(HSQLDB)
@TrueSqlTests2.Message(kind = ERROR, text = "Expected identifier for source for `.constraint(...)`")
@TrueSql public class __44__ConstraintUnresolvedSource {
    static DataSourceW f(DataSourceW ds) {
        return ds;
    }
    @TestTemplate public void test(MainDataSource ds) {
        f(ds).constraint("", "", null);
    }
}
