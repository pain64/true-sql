package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __27__ToDtoUnchecked {

    public record City(Long id, String name) { }

    @TestTemplate public void test(MainDataSourceUnchecked ds) throws JsonProcessingException {
        Assertions.assertEquals(
            new City(1L, "London"),
            ds.q("select * from city where id = 1").fetchOne(City.class)
        );
    }
}
