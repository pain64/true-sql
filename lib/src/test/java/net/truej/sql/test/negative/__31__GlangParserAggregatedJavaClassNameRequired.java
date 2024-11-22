package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto2;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.Message(
    kind = ERROR, text = "Aggregated java class name required"
) @TrueSqlTests2.EnableOn(POSTGRESQL) @Disabled
@TrueSql public class __31__GlangParserAggregatedJavaClassNameRequired {
    @TestTemplate public void test2(MainConnection cn) {
        cn.q("""
            select
                1 as "a", 
                1 as " b.", 
                1 as " b.c"  
            """).g.fetchOne(Dto2.class);
    }
}
