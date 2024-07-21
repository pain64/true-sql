package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "To Dto class cannot be interface"
)
@TrueSql public class __08__InterfaceAsDto {
    interface XXX { }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(XXX.class);
    }
}
