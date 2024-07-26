package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.DisabledOn;
import net.truej.sql.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.source.Parameters.NotNull;

// FIXME: test for hsqldb without in no-checks mode
@ExtendWith(TrueSqlTests2.class) @DisabledOn(HSQLDB)
@TrueSql public class __08__GeneratedKeysOneColumn {

    @TestTemplate public void test(MainDataSource ds) {
        Assertions.assertEquals(
            3L, ds.q("insert into users(name, info) values(?, ?)", "Boris", null)
                .asGeneratedKeys("id").fetchOne(NotNull, Long.class)
        );
    }
}
