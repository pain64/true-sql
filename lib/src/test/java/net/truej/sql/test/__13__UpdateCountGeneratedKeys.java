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
import static net.truej.sql.compiler.TrueSqlTests2.DisabledOn;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __13__UpdateCountGeneratedKeys {

    @TestTemplate @DisabledOn(HSQLDB)
    public void withGeneratedKeys(MainConnection cn) {
        //TODO: fetch stream with g?
        try (var result = cn.q("insert into users values(4, 'Mike', null)")
            .asGeneratedKeys("id").withUpdateCount.fetchStream(Long.class)) {

            System.out.println(result);
        }
    }
}
