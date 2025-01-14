package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.compiler.TrueSqlTests.DisabledOn;
import static net.truej.sql.fetch.Parameters.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TrueSqlTests.class) @DisabledOn({HSQLDB, MSSQL})
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
