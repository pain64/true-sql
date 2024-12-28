package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto1;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "compile time checks must be enabled for .g")
@TrueSql public class __34__CompileTimeChecksMustBeEnabledForG {

    @TestTemplate public void test(MainDataSourceUnchecked ds) {
        ds.q("values 1").g.fetchOne(Dto1.class);
    }
}
