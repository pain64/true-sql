package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.fetch.Parameters;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Message;

@ExtendWith(TrueSqlTests2.class) @Message(
    kind = ERROR, text = "Incorrect TrueSql DSL usage - dangling call"
)
@TrueSql public class __06__DanglingCall_Parameters {

    @TestTemplate public void test(MainConnection cn) {
        Parameters.out(String.class);
    }
}
