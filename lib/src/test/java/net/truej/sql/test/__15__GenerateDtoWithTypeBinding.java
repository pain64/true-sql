package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.test.__05__GenerateDtoG.User5;
import net.truej.sql.test.__05__GenerateDtoG.User6;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.POSTGRESQL;
import static net.truej.sql.util.Formatter.pretty;

// FIXME
// Inverse this test and port to other databases (not HSQLDB)
@ExtendWith(TrueSqlTests.class) @EnableOn(POSTGRESQL)
@TrueSql public class __15__GenerateDtoWithTypeBinding {
    @TestTemplate public void test5(MainDataSource ds) {
        Assertions.assertEquals(
            """
                User5[
                  name=Joe, sex=MALE]
                """,
            pretty(
                ds.q("""
                    select name, sex as ":t UserSex sex" from users where id = 1"""
                ).g.fetchOne(User5.class)
            )
        );
    }
    @TestTemplate public void test6(MainDataSource ds) {
        Assertions.assertEquals(
            """
                User6[
                  name=Joe, sex=MALE]
                """,
            pretty(
                ds.q("""
                    select name, sex as ":t UserSex! sex" from users where id = 1"""
                ).g.fetchOne(User6.class)
            )
        );
    }
}
