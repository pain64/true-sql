package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.test.__05__GenerateDtoTrueSql.ByteArrayWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @EnableOn(POSTGRESQL)
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
