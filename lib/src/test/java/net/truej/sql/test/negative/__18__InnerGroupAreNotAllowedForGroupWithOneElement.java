package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.Clinic2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "Inner groups not allowed for group with one element"
)
@TrueSql public class __18__InnerGroupAreNotAllowedForGroupWithOneElement {
    @TestTemplate
    public void test(MainConnection cn) {

//        cn.q("""
//            select id as "ab.User Hi" from users"""
//        ).g.fetchList(Clinic2.class);
        cn.q("""
                    select
                        c.id   as		"  id ",
                        c.name as 		"  name      ",
                        u.id   as 		" users.id.here"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id"""
        ).g.fetchList(Clinic2.class);

    }
}
