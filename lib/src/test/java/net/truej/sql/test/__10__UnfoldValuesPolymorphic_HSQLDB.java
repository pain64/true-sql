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
import static net.truej.sql.source.Parameters.NotNull;
import static net.truej.sql.source.Parameters.unfold;

// ? DB2
// -  MySQL
// + MariaDB - cte + custom type inference
// + MSSQL - custom type inference
// + PostgreSQL - offset workaround
// + HSQLDB - toString + cast
// + Oracle 23ai - не проверять типы UNSPECIFIED параметров
// - Oracle 12   - пока не поддерживаем
@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@TrueSql public class __10__UnfoldValuesPolymorphic_HSQLDB {
    @TestTemplate public void unfold_workaround(MainDataSource ds) {
        var ids = List.of(1, 2, 3);

        Assertions.assertEquals(
            List.of(1, 2, 3),
            ds.q("""
                select cast(x as int) from (values ?) as v(x)
                """, unfold(ids, id -> new Object[]{id.toString()})
            ).fetchList(NotNull, Integer.class)
        );
    }
}
