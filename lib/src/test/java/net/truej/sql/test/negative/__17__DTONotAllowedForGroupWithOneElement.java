package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.Clinic2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class)
@Message(
    kind = ERROR, text = "Dto class name not allowed for group with one element - thees groups converts to List<single group element class name>"
)
@EnableOn(HSQLDB) @TrueSql public class __17__DTONotAllowedForGroupWithOneElement {
    @TestTemplate
    public void test(MainConnection cn) {
        cn.q("""
                    select
                        c.id   as		"  id ",
                        c.name as 		"  name      ",
                        u.id   as 		" User2 users.id"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id"""
        ).g.fetchList(Clinic2.class);
    }
}
