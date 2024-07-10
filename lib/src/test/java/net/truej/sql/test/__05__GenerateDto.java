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
        ).g.fetchList(User1.class);

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
                    [\
                    {"id":1,"name":"Paris Neurology Hospital","users":[{"id":2,"name":"Donald"}]},\
                    {"id":2,"name":"London Heart Hospital","users":[{"id":1,"name":"Joe"}]},\
                    {"id":3,"name":"Diagnostic center","users":[{"id":0,"name":null}]}\
                    ]""",
                new ObjectMapper().writeValueAsString(
                        cn.q("""
                            select
                                c.id   as		"id",
                                c.name as 		"name",
                                u.id   as 		"User2 users.id",
                                u.name as 		"     users.name"
                            from clinic c
                                left join clinic_users cu on cu.clinic_id = c.id
                                left join user u on u.id = cu.user_id
                        """).g.fetchList(Clinic.class)
                )
        );
    }

    @Test public void test3(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [\
                {"id":1,"name":"Paris Neurology Hospital","users":[{"id":2,"name":"Donald"}]},\
                {"id":2,"name":"London Heart Hospital","users":[{"id":1,"name":"Joe"}]},\
                {"id":3,"name":"Diagnostic center","users":[{"id":0,"name":null}]}\
                ]""",
            new ObjectMapper().writeValueAsString(
                cn.q("""
                            select
                                ci.name as "city",
                                cl.name as "clinic.",
                                u.name as  "User1 users.name",
                                u.info as  "      users.info",
                                b.date as  "      users.Bill bills.date",
                                b.amount as "     users.     bills.amount"
                            from city ci
                                join clinic cl on ci.id = cl.city_id
                                left join clinic_users clu on clu.clinic_id = cl.id
                                left join user u on clu.user_id = u.id
                                left join user_bills ub on ub.user_id = u.id
                                left join bill b on b.id = ub.bill_id
                """).g.fetchList(Clinic2.class)
            )
        );
    }
}