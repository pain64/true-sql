package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.test.__05__GenerateDtoG.ByteArrayWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class) @EnableOn(POSTGRESQL)
@TrueSql public class __05__GenerateDto_ByteArray__POSTGRESQL {
        @TestTemplate public void test(MainConnection cn) throws JsonProcessingException {
            Assertions.assertEquals(
                """
                   [ {
                     "data" : "AQID"
                   } ]""",
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                    cn.q("select decode('010203', 'hex') as data")
                        .g.fetchList(ByteArrayWrapper.class)
                )
            );
        }
}
