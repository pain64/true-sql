package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.test.__05__GenerateDtoTrueSql.User5;
import net.truej.sql.test.__05__GenerateDtoTrueSql.User6;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __15__GenerateDtoWithTypeBinding {
    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB)
    public void test5(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                {
                  "name" : "Joe",
                  "sex" : "MALE"
                }""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                    select name, sex as ":t UserSex sex" from users where id = 1""").g.fetchOne(User5.class)
                )
        );
    }
    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB)
    public void test6(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            """
                {
                  "name" : "Joe",
                  "sex" : "MALE"
                }""",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                    select name, sex as ":t UserSex! sex" from users where id = 1""").g.fetchOne(User6.class)
                )
        );
    }
}
