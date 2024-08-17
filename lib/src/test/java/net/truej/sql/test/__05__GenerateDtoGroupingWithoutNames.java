package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;


import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.test.__05__GenerateDtoGroupingWithoutNamesTrueSql.*;

@ExtendWith(TrueSqlTests2.class) @DisabledOn({MSSQL, ORACLE, MYSQL, MARIADB})
// FIX test for enable on MySQL: mysql has no OffsetDateTime
@TrueSql public class __05__GenerateDtoGroupingWithoutNames {

    @TestTemplate
    public void groupingWithoutNames(MainConnection cn) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                [ {
                  "name" : "Joe",
                  "info" : null,
                  "bills" : [ {
                    "date" : 1719835200.000000000,
                    "amount" : 2000.55
                  }, {
                    "date" : 1719849600.000000000,
                    "amount" : 1000.20
                  } ]
                }, {
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
                } ]""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    cn.q("""
                        select
                            u.name                   ,
                            u.info                   ,
                            b.date   as "Bill2 bills.",
                            b.amount as "     bills."
                        from users u
                            left join user_bills   ub  on ub.user_id    = u.id
                            left join bill         b   on b.id          = ub.bill_id
                        order by u.id"""
                    ).g.fetchList(User.class)
                )
        );
    }
}
