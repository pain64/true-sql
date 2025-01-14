package net.truej.sql.test.bindings;


import net.truej.sql.TrueSql;
import net.truej.sql.bindings.OffsetTimeReadWrite;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(HSQLDB)
@TrueSql public class __01__OffsetTimeCoverage {
    //There is NO db  (drivers) supporting OffsetTime type
    //need coverage
    @TestTemplate public void test() {
        var a = new OffsetTimeReadWrite();
        a.sqlType();
    }
}
