package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.Clinic;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "Expected t but hasText[t=v]"
)
@TrueSql public class __09__WrongGLex_t {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
                    select
                        c.id   as		"          id      ",
                        c.name as 		"          name      ",
                        u.id   as 		":v! User2 users.id  ",
                        u.name as 		":t!       users.name"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id"""
        ).g.fetchList(Clinic.class);
    }

}
