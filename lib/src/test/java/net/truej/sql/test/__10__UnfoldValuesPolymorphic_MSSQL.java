package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.MSSQL;
import static net.truej.sql.source.Parameters.NotNull;
import static net.truej.sql.source.Parameters.unfold;


@ExtendWith(TrueSqlTests2.class) @EnableOn(MSSQL)
@TrueSql public class __10__UnfoldValuesPolymorphic_MSSQL {
    @TestTemplate public void unfold_values(MainDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of(1, 2, 3),
            ds.q(
                "select cast(x as int) from (values ?) as v(x)", unfold(ids)
            ).fetchList(NotNull, Integer.class)
        );
    }
}
