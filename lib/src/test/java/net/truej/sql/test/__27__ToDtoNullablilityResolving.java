package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.fetch.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.POSTGRESQL;
import static net.truej.sql.fetch.Parameters.NotNull;
import static net.truej.sql.fetch.Parameters.Nullable;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(POSTGRESQL)
@TrueSql
public class __27__ToDtoNullablilityResolving {
    @TestTemplate public void testStaticImport(MainDataSource ds) {
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(Nullable, Integer.class)
        );
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(NotNull, Integer.class)
        );
    }

    @TestTemplate public void testSimpleNameImport(MainDataSource ds) {
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(Parameters.Nullable, Integer.class)
        );
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(Parameters.NotNull, Integer.class)
        );
    }

    @TestTemplate public void testFullNameImport(MainDataSource ds) {
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(net.truej.sql.fetch.Parameters.Nullable, Integer.class)
        );
        Assertions.assertEquals(
            1,
            ds.q("select 1").fetchOne(net.truej.sql.fetch.Parameters.NotNull, Integer.class)
        );
    }
}
