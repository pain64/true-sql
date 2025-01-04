package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class)
@Message(
    kind = ERROR, text = "has no type binding for net.truej.sql.test.negative." +
                         "__07__NoTypeBindingExistingDto_$User$Trap"
) @EnableOn(HSQLDB)
@TrueSql public class __07__NoTypeBindingExistingDto {
    record User(Long id, String name, Trap trap) {
        record Trap(String name) {}
    }
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select * from users").fetchList(User.class);
    }
}
