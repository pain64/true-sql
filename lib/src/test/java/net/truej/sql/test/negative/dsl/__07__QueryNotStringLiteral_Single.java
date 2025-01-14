package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @Message(
    kind = ERROR, text = "Query text must be a string literal"
)
@EnableOn(HSQLDB) @TrueSql public class __07__QueryNotStringLiteral_Single {

    @TestTemplate public void test(MainConnection cn) {
        cn.q("a" + 1).fetchNone();
    }
}
