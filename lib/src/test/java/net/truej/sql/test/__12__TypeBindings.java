package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.geometric.PGpoint;


import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __12__TypeBindings {

    @TestTemplate @DisabledOn(HSQLDB)
    public void testPointBind(MainDataSource ds) {
        var expected = new PGpoint(1, 1);

        Assertions.assertEquals(
            expected, ds.q("select ?::point", expected).fetchOne(PGpoint.class)
        );
    }
}
