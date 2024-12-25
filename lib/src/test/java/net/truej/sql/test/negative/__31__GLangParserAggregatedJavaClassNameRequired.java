package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto2;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "Aggregated java class name required"
) @EnableOn(POSTGRESQL)
@TrueSql public class __31__GLangParserAggregatedJavaClassNameRequired {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
            select
                1 as "a", 
                1 as " b.", 
                1 as " b.c"  
            """).g.fetchOne(Dto2.class);
    }
}
