package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@Disabled
@ExtendWith(TrueSqlTests2.class)
//@TrueSqlTests2.Message(
//    kind = ERROR, text = "target type implies 1 columns but result has 3"
//)
@TrueSql public class __21__TypeMismatch {
//    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB) public void test(MainConnection cn) {
//        cn.q("""
//            select point(1,1)::point as ":t UserSex sex" from users where id = 1
//            """).g.fetchNone();
//    }
}
