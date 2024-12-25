package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;

import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class)
@Message(
    kind = ERROR, text = "Expected %NewDtoClassName%.class"
)
@EnableOn(POSTGRESQL)
@TrueSql public class __32__GenerateDtoExpectedNewDtoClassName {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select 1").g.fetchOne(__30__GenerateDtoTrueSql.BadTypeDto.class);
    }
}
