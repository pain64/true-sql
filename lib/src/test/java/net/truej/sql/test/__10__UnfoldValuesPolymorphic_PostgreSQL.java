package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import static net.truej.sql.source.Parameters.Nullable;
import static net.truej.sql.source.Parameters.unfold;

@ExtendWith(TrueSqlTests2.class) @EnableOn(POSTGRESQL)
@TrueSql public class __10__UnfoldValuesPolymorphic_PostgreSQL {

    @TestTemplate public void unfold_values_workaround1(MainDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of(1, 2, 3),
            ds.q(
                "select x from (values (1), ? offset 1) as v(x)", unfold(ids)
            ).fetchList(Nullable, Integer.class)
        );
    }

    @TestTemplate public void unfold_values_workaround2(MainDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of(1, 2, 3),
            ds.q(
                "select x::int from (values ?) as v(x)",
                unfold(ids, id -> new Object[]{id.toString()})
            ).fetchList(Nullable, Integer.class)
        );
    }
}
