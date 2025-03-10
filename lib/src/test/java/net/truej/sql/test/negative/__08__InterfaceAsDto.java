package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "To Dto class cannot be interface"
)
@EnableOn(HSQLDB) @TrueSql public class __08__InterfaceAsDto {
    interface XXX { }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(XXX.class);
    }
}
