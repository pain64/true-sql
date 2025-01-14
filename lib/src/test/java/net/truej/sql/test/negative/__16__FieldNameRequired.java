package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.Clinic;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class)
@EnableOn({ORACLE, MSSQL})
@Message(
    kind = ERROR, text = "Your database driver doest not provides column name" +
                         " (labels only). Field name required"
)
@TrueSql public class __16__FieldNameRequired {

    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
            select
                c.id                          ,
                c.name                        ,
                u.id   as ":t! User2 users.id",
                u.name as ":t!       users."
            from clinic c
                left join clinic_users cu on cu.clinic_id = c.id
                left join users         u on u.id         = cu.user_id"""
        ).g.fetchList(Clinic.class);
    }
}
