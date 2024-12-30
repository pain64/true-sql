package net.truej.sql.test.bindings;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.MapperGenerator;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.MYSQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(MYSQL)
@TrueSql public class __03__CharTest {
    @TestTemplate
    public void test(MainDataSource ds) {
        var mg = new MapperGenerator();
        Assertions.assertEquals(
            Character.class.getName(),
            mg..
            );
    }

}
