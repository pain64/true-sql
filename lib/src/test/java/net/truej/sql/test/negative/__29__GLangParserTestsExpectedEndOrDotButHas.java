package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto1;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class)
@TrueSqlTests2.Message(
    kind = ERROR, text = "expected END or DOT but has TEXT(text)"
)
@TrueSqlTests2.Message(
    kind = ERROR, text = "expected END or DOT or TEXT but has QUESTION_MARK"
)
@TrueSqlTests2.Message(
    kind = ERROR, text = "expected END or DOT but has COLON"
)
@TrueSqlTests2.Message(
    kind = ERROR, text = "Expected t but has END"
)

@TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSql public class __29__GLangParserTestsExpectedEndOrDotButHas  {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
                select 1 as "Text text text" """).g.fetchOne(Dto1.class);

        cn.q("""
                select 1 as "Text?" """).g.fetchOne(Dto1.class);

        cn.q("""
                select 1 as "Text text :" """).g.fetchOne(Dto1.class);

        cn.q("""
                select 1 as ":" """).g.fetchOne(Dto1.class);
    }


    //Field name required is not accessible?
}