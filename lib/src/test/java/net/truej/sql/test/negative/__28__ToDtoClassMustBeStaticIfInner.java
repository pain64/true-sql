package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "To Dto class must be static if inner")
@TrueSql public class __28__ToDtoClassMustBeStaticIfInner {

    class BadClass {
        BadClass(int value) { }
    }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("values 1").fetchOne(BadClass.class);
    }
}
