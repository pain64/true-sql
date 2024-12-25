package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "unexpected constructor parameter of kind: TYP")
@TrueSql public class __33__UnexpectedDtoConstructorParameterKind {

    static class BadClass {
        <T> BadClass(T value) { }
    }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("values 1").fetchOne(BadClass.class);
    }
}
