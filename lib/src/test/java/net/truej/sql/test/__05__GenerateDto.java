package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoTrueSql.*;

import java.util.HashMap;
import java.util.function.Supplier;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @DisabledOn({ORACLE, MYSQL, MARIADB})
// FIX test for enable on MySQL: mysql has no OffsetDateTime
@TrueSql public class __05__GenerateDto {

    @TestTemplate public void test(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "id" : 1,
                  "name" : "Joe",
                  "info" : null
                }, {
                  "id" : 2,
                  "name" : "Donald",
                  "info" : "Do not disturb"
                } ]""",
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                cn.q("select id, name, info from users").g.fetchList(User.class)
            )
        );

        var getUser = (Supplier<Object>) () ->
            cn.q("select id, name, info from users where id = 1").g.fetchOne(User1.class);

        var u1 = getUser.get();
        var u2 = getUser.get();

        // check generated equals
        Assertions.assertEquals(u1, u2);

        // check generated equals and hashCode
        var hmap = new HashMap<Object, String>();

        hmap.put(u1, "True");
        hmap.put(u2, "Sql");

        Assertions.assertEquals(hmap.get(u1), "Sql");
        Assertions.assertEquals(hmap.get(u2), "Sql");
    }

    @TestTemplate public void test2(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "id" : 1,
                  "name" : "Paris Neurology Hospital",
                  "users" : [ {
                    "id" : 2,
                    "name" : "Donald"
                  } ]
                }, {
                  "id" : 2,
                  "name" : "London Heart Hospital",
                  "users" : [ {
                    "id" : 1,
                    "name" : "Joe"
                  } ]
                }, {
                  "id" : 3,
                  "name" : "Diagnostic center",
                  "users" : [ ]
                } ]""",
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                cn.q("""
                    select
                        c.id   as		"          id        ",
                        c.name as 		"          name      ",
                        u.id   as 		":t! User2 users.id  ",
                        u.name as 		":t!       users.name"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id"""
                ).g.fetchList(Clinic.class)
            )
        );
    }

    @TestTemplate public void test3(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "city" : "London",
                  "clinic" : [ "Diagnostic center", "London Heart Hospital" ],
                  "users" : [ {
                    "name" : "Joe",
                    "info" : null,
                    "bills" : [ {
                      "date" : 1719835200.000000000,
                      "amount" : 2000.55
                    }, {
                      "date" : 1719849600.000000000,
                      "amount" : 1000.20
                    } ]
                  } ]
                }, {
                  "city" : "Paris",
                  "clinic" : [ "Paris Neurology Hospital" ],
                  "users" : [ {
                    "name" : "Donald",
                    "info" : "Do not disturb",
                    "bills" : [ {
                      "date" : 1722524400.000000000,
                      "amount" : 5000.00
                    }, {
                      "date" : 1722524400.000000000,
                      "amount" : 7000.77
                    }, {
                      "date" : 1725202800.000000000,
                      "amount" : 500.10
                    } ]
                  } ]
                } ]""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    cn.q("""
                        select
                            ci.name  as "      city                   ",
                            cl.name  as "      clinic.                ",
                            u.name   as "User3 users .name            ",
                            u.info   as "      users .info            ",
                            b.date   as "      users .Bill bills.date ",
                            b.amount as "      users .     bills.amount"
                        from city ci
                                 join clinic       cl  on ci.id         = cl.city_id
                            left join clinic_users clu on clu.clinic_id = cl.id
                            left join users        u   on clu.user_id   = u.id
                            left join user_bills   ub  on ub.user_id    = u.id
                            left join bill         b   on b.id          = ub.bill_id
                        order by ci.name, cl.name, u.name, u.info, b.date, b.amount"""
                    ).g.fetchList(Clinic2.class)
                )
        );
    }

    @TestTemplate public void keepCaseInAsNames(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "userName" : "Joe",
                  "userInfo" : null
                }, {
                  "userName" : "Donald",
                  "userInfo" : "Do not disturb"
                } ]""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                        select
                            u.name as "userName",
                            u.info as "userInfo"
                        from users u order by u.id"""
                    ).g.fetchList(User4.class)
                )
        );
    }

    @TestTemplate public void test5(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "name" : "Joe",
                  "info" : null
                }, {
                  "name" : "Donald",
                  "info" : "Do not disturb"
                } ]""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                        select name, info as ":t? info" from users"""
                    ).g.fetchList(User5.class)
                )
        );
    }
}