package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __07__WithConnection {

    @TestTemplate @DisabledOn(HSQLDB) public void test(MainDataSource ds) {
        var expectedTimeZone = "America/New_York";
        Assertions.assertEquals(
            expectedTimeZone,
            ds.withConnection(cn -> {
                    cn.q("set time zone 'America/New_York'").fetchNone();

                    return cn.q("select current_setting('TIMEZONE')").fetchOne(String.class);
                }
            )
        );

    }
}
