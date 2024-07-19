package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.geometric.PGpoint;

@ExtendWith(TrueSqlTests2.class)
@Disabled
@TrueSql public class __12__TypeBindings {

    @TestTemplate @TrueSqlTests2.DisabledOn(TrueSqlTests2.Database.HSQLDB)
    public void testPointBind(MainDataSource ds) {
        var expected = new PGpoint(1,1);

        Assertions.assertEquals(
            expected,
            ds.q("select ?", expected).fetchOne(PGpoint.class)
        );
    }


}
