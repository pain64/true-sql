package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Modifier;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __28_1__GenerateDtoPackagePrivate {

    @TestTemplate public void test(MainDataSourceUnchecked ds) throws ClassNotFoundException {
        Assertions.assertTrue(
            (Class.forName(this.getClass().getName() + "G")
            .getModifiers() & Modifier.PUBLIC) > 0
        );
    }
}
