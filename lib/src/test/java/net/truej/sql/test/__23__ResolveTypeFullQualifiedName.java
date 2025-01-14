package net.truej.sql.test;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.UserSex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.POSTGRESQL;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(POSTGRESQL)
@TrueSql public class __23__ResolveTypeFullQualifiedName {

    @TestTemplate public void test1(MainConnection cn) {
        Assertions.assertEquals(
            "Joe",
            cn.q(
                "select name from users where id = ?", 1L
            ).fetchOne(java.lang.String.class)
        );
    }

    public record A(String v) {}

    @TestTemplate public void test2(MainDataSource ds) {
        Assertions.assertEquals(
            UserSex.MALE,
            ds.q(
                "select sex from users where id = ?", 1L
            ).fetchOne(NotNull, net.truej.sql.compiler.UserSex.class)
        );
    }
}
