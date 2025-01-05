package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDtoG.SC;

import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSql public class __26__SnakeCaseToCamelCase {
    @TestTemplate public void test(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals("""
            {
              "helloItsMe" : 1
            }""",
            new ObjectMapper()
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
            select 1 as "hello_its_me"
            """).g.fetchOne(SC.class))
        );
    }
}
