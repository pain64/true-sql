package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.PgDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __08__GeneratedKeys {
    @Test public void test(PgDataSource ds) {

        var a = ds.q("select name from users").fetchList(String.class);
        System.out.println(a);
    }
}
