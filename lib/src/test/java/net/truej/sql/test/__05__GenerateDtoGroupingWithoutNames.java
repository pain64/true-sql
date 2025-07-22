package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;


import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.test.__05__GenerateDtoGroupingWithoutNamesTrueSql.*;
import static net.truej.sql.util.Formatter.pretty;

@ExtendWith(TrueSqlTests.class) @DisabledOn({MSSQL, ORACLE, MYSQL, MARIADB})
// FIX test for enable on MySQL: mysql has no OffsetDateTime
@TrueSql public class __05__GenerateDtoGroupingWithoutNames {
    @TestTemplate public void groupingWithoutNames(MainConnection cn) {
        Assertions.assertEquals(
            """
              [
                User[
                  name=Joe, info=null, bills=[
                    Bill2[
                      id=1, date=2024-07-01T12:00Z, amount=2000.55]
                    , Bill2[
                      id=2, date=2024-07-01T16:00Z, amount=1000.20]
                    ]
                  ]
                , User[
                  name=Donald, info=Do not disturb, bills=[
                    Bill2[
                      id=3, date=2024-08-01T15:00Z, amount=5000.00]
                    , Bill2[
                      id=4, date=2024-08-01T15:00Z, amount=7000.77]
                    , Bill2[
                      id=5, date=2024-09-01T15:00Z, amount=500.10]
                    ]
                  ]
                ]
              """,
            pretty(
                cn.q("""
                    select
                        u.name                   ,
                        u.info                   ,
                        b.id     as "Bill2 bills.",
                        b.date   as "      bills.",
                        b.amount as "      bills."
                    from users u
                        left join user_bills   ub  on ub.user_id    = u.id
                        left join bill         b   on b.id          = ub.bill_id
                    order by u.id"""
                ).g.fetchList(User.class)
            )
        );
    }
}
