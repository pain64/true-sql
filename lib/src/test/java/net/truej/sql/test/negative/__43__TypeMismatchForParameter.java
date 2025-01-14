package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "type mismatch for parameter 1. " +
                              "Expected java.lang.Long but has java.lang.String")
@TrueSql public class __43__TypeMismatchForParameter {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("insert into users(id, name) values(?, ?)", "", "").fetchNone();
    }
}
