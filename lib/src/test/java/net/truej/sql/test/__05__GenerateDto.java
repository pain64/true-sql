package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoTrueSql.*;

import java.util.HashMap;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __05__GenerateDto {
    @Test public void test(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [{"ID":1,"NAME":"Joe","INFO":null},{"ID":2,"NAME":"Donald","INFO":"Do not disturb"}]""",
            new ObjectMapper().writeValueAsString(
                cn.q("select * from user").g.fetchList(User.class)
            )
        );

        var users = cn.q("""
            select * from user where id = 1
            union all
            select * from user where id = 1"""
        ).g.fetchList(NUser.class);

        // check generated equals
        Assertions.assertEquals(users.get(0), users.get(1));

        // check generated equals and hashCode
        var hmap = new HashMap<Object, String>();

        hmap.put(users.get(0), "True");
        hmap.put(users.get(1), "Sql");

        Assertions.assertEquals(hmap.get(users.get(0)), "Sql");
        Assertions.assertEquals(hmap.get(users.get(1)), "Sql");
    }
    @Test public void test2(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
                """
                    [{"ID":1,"NAME":"Joe","INFO":null},{"ID":2,"NAME":"Donald","INFO":"Do not disturb"}]""",
                new ObjectMapper().writeValueAsString(
                        cn.q("""
                            select
                                c.id   as		"id",
                                c.name as 		"name",
                                u.id   as 		"User users.id",
                                u.name as 		"     users.name"
                            from clinic c
                                left join clinic_users cu on cu.clinic_id = c.id
                                left join user u on u.id = cu.user_id
                        """).g.fetchList(Clinic.class)
                )
        );

    }
}