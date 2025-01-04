package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "expected Name.class or full.qualified.Name.class or array[].class"
) @EnableOn(HSQLDB)
@TrueSql public class __13__FetchTo_IsNotClassLiteral_FieldAccess {
    class AAA {
        static Class<String> xxx = String.class;
    }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("select id from users").fetchOne(AAA.xxx);
    }
}