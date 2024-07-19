package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "has no type binding for net.truej.sql.test.negative.__07__NoTypeBinding_.User.Trap"
)
@TrueSql public class __07__NoTypeBinding {
    record User(Long id, String name, Trap trap) {
        record Trap(String name) {}
    }
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(User.class);
    }
}
