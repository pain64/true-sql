package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.compiler.UserSex;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.MYSQL;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(MYSQL)
@TrueSqlTests.Message(kind = ERROR, text = "sql type id (java.sql.Types) mismatch for column 1 (for field `result`)." +
                                           " Expected 12 but has -5")
@TrueSql public class __36__TypeCheckerTypeMismatchSqlType {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
            select 1
            """).fetchOne(UserSex.class);
    }
}
