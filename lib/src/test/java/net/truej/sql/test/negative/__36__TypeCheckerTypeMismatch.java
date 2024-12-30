package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.geometric.PGpoint;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSqlTests2.Message(kind = ERROR, text = "sql type name mismatch for column 1 (for field `result`)." +
    " Expected point but has int4")
@TrueSql public class __36__TypeCheckerTypeMismatch {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
            select 1
            """).fetchOne(PGpoint.class);
    }
}