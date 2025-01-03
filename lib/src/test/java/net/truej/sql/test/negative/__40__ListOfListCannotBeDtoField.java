package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "value of List<List<...>> cannot be Dto field")
@TrueSql public class __40__ListOfListCannotBeDtoField {
    record A(List<List<String>> v) { }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("values 1").fetchOne(A.class);
    }
}
