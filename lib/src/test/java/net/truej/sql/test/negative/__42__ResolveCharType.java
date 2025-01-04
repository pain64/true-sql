package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto5;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.MYSQL;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "has no type binding for char")
@TrueSql public class __42__ResolveCharType {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("values 'xx'").fetchOne(char.class);
    }
}
