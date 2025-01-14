package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.Clinic;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

@ExtendWith(TrueSqlTests.class)
@TrueSqlTests.Message(
    kind = ERROR, text = "expected END or TEXT but has DOT"
)
@TrueSql public class __11__WrongG_Expected_END_or_TEXT_but_has {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
                    select
                        c.id   as		"          id      ",
                        c.name as 		"          name      ",
                        u.id   as 		":t! . users.id",
                        u.name as 		":t!       users.name"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id"""
        ).g.fetchList(Clinic.class);
    }
}
