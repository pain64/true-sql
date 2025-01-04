package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto5;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.MYSQL;

@ExtendWith(TrueSqlTests2.class) @EnableOn(MYSQL)
@Message(kind = ERROR, text = "Your database driver doest not provides column" +
                              " name (labels only). Field name required")
@TrueSql public class __41__MySqlNullColumnLabel {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("select 1 as \"\"").g.fetchOne(Dto5.class);
    }
}
