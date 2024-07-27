package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.MSSQL;
import static net.truej.sql.compiler.TrueSqlTests2.DisabledOn;
import static net.truej.sql.source.Parameters.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TrueSqlTests2.class) @DisabledOn({HSQLDB, MSSQL})
@TrueSql public class __13__UpdateCountGeneratedKeys {

    @TestTemplate public void withGeneratedKeys(MainConnection cn) {
        //TODO: fetch stream with g?
        try (var result = cn.q("insert into users(name, info) values('Mike', null)")
            .asGeneratedKeys("id").withUpdateCount.fetchStream(NotNull, Long.class)) {

            assertEquals(1L, result.updateCount);
            assertEquals(List.of(3L), result.value.toList());

            System.out.println(result);
        }
    }
}
