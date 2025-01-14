package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(HSQLDB)
@TrueSqlTests.Message(kind = ERROR, text = "constraint not found")
@TrueSql public class __45__ConstraintNotFound {
    @TestTemplate public void test(MainDataSource ds) {
        ds.constraint("privet", "privet_pk", null);
    }
}
