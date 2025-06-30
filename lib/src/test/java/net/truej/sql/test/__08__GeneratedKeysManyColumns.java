package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.DisabledOn;
import net.truej.sql.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.fetch.Parameters.NotNull;

// FIXME: check MariaDB insert-returning
@ExtendWith(TrueSqlTests.class) @DisabledOn({MYSQL, MARIADB, MSSQL})
@TrueSql public class __08__GeneratedKeysManyColumns {

    record IdAndName(long id, String name) { }

    @TestTemplate public void test(MainConnection cn) throws SQLException {
        cn.w.setAutoCommit(false);

        try {
            Assertions.assertEquals(
                new IdAndName(3L, "Boris"),
                cn.q("insert into users values(?, ?, ?)", 3L, "Boris", null)
                    .asGeneratedKeys("id", "name").fetchOne(IdAndName.class)
            );
        } finally {
            cn.w.rollback();
            cn.w.setAutoCommit(true);
        }
    }
}
