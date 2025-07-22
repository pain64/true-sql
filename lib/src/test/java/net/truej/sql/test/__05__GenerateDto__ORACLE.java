package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.test.__05__GenerateDtoG.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.function.Supplier;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.util.Formatter.pretty;

@ExtendWith(TrueSqlTests.class) @EnableOn(ORACLE)
@TrueSql public class __05__GenerateDto__ORACLE {

    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertEquals(
            """
                [
                  User[
                    id=1, name=Joe, info=null]
                  , User[
                    id=2, name=Donald, info=Do not disturb]
                  ]
                """,
            pretty(
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

    @TestTemplate public void test2(MainConnection cn) {
        Assertions.assertEquals(
            """
                [
                  Clinic[
                    id=1, name=Paris Neurology Hospital, users=[
                      User2[
                        id=2, name=Donald]
                      ]
                    ]
                  , Clinic[
                    id=2, name=London Heart Hospital, users=[
                      User2[
                        id=1, name=Joe]
                      ]
                    ]
                  , Clinic[
                    id=3, name=Diagnostic center, users=[
                      ]
                    ]
                  ]
                """,
            pretty(
                cn.q("""
                    select
                        c.id   as		"          id        ",
                        c.name as 		"          name      ",
                        u.id   as 		":t! User2 users.id  ",
                        u.name as 		":t!       users.name"
                    from clinic c
                        left join clinic_users cu on cu.clinic_id = c.id
                        left join users         u on u.id         = cu.user_id
                    order by c.id, c.name, u.id, u.name"""
                ).g.fetchList(Clinic.class)
            )
        );
    }

    @TestTemplate public void test3(MainConnection cn) {
        Assertions.assertEquals(
            """
                [
                  Clinic2[
                    city=London, clinic=[
                      Diagnostic center, London Heart Hospital]
                    , users=[
                      User3[
                        name=Joe, info=null, bills=[
                          Bill[
                            id=1, date=2024-07-01T12:00Z[
                              UTC]
                            , amount=2000.55]
                          , Bill[
                            id=2, date=2024-07-01T16:00Z[
                              UTC]
                            , amount=1000.2]
                          ]
                        ]
                      ]
                    ]
                  , Clinic2[
                    city=Paris, clinic=[
                      Paris Neurology Hospital]
                    , users=[
                      User3[
                        name=Donald, info=Do not disturb, bills=[
                          Bill[
                            id=3, date=2024-08-01T15:00Z[
                              UTC]
                            , amount=5000]
                          , Bill[
                            id=4, date=2024-08-01T15:00Z[
                              UTC]
                            , amount=7000.77]
                          , Bill[
                            id=5, date=2024-09-01T15:00Z[
                              UTC]
                            , amount=500.1]
                          ]
                        ]
                      ]
                    ]
                  ]
                """,
            pretty(
                cn.q("""
                    select
                        ci.name    as "      city                    ",
                        cl.name    as "      clinic.                 ",
                        u.name     as "User3 users .name             ",
                        u.info     as "      users .info             ",
                        b.id       as "      users .Bill bills.id    ",
                        b."date"   as "      users .     bills.date  ",
                        b.amount   as "      users .     bills.amount"
                    from city ci
                             join clinic       cl  on ci.id         = cl.city_id
                        left join clinic_users clu on clu.clinic_id = cl.id
                        left join users        u   on clu.user_id   = u.id
                        left join user_bills   ub  on ub.user_id    = u.id
                        left join bill         b   on b.id          = ub.bill_id
                    order by ci.name, cl.name, u.name, u.info, b."date", b.amount"""
                ).g.fetchList(Clinic2.class)
            )
        );
    }

    @TestTemplate public void test4(MainDataSource ds) {
        Assertions.assertEquals(
            """
              [
                User4[
                  name=Joe, info=null]
                , User4[
                  name=Donald, info=Do not disturb]
                ]
              """,
            pretty(
                ds.q("""
                    select name, info as ":t? info" from users"""
                ).g.fetchList(User4.class)
            )
        );
    }
}