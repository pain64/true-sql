package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.UserSex;
import net.truej.sql.source.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.source.Parameters.*;
import static net.truej.sql.source.Parameters.unfold4;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __14__UnfoldMany {
    @TestTemplate
    @TrueSqlTests2.DisabledOn(HSQLDB) public void unfoldMany2(MainDataSource ds) {
        var data = List.of(
            new Parameters.Pair<>(1L, "Joe"),
            new Parameters.Pair<>(2L, "Donald")
        );

        Assertions.assertEquals(
            List.of("null", "Do not disturb"),
            ds.q("""
                select case when info is null then 'null' else info end as info
                from users 
                where (id, name) in (?)""", unfold2(data))
                .fetchList(Nullable, String.class)
        );
    }

    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB) public void unfoldMany3(MainDataSource ds) {
        var data = List.of(
            new Parameters.Triple<Long, String, String>(1L, "Joe", null),
            new Parameters.Triple<>(2L, "Donald", "Do not disturb")
        );

        Assertions.assertEquals(
            List.of("Donald"),
            ds.q("select name from users where (id, name, info) in (?)", unfold3(data))
                .fetchList(String.class)
        );
    }

    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB) public void unfoldMany4(MainDataSource ds) {
        var data = List.of(
            new Quad<Long, String, String, UserSex>(1L, "Joe", null, UserSex.MALE),
            new Quad<>(2L, "Donald", "Do not disturb", UserSex.MALE)
        );

        Assertions.assertEquals(
            List.of("Donald"),
            ds.q("select name from users where (id, name, info, sex) in (?)", unfold4(data))
                .fetchList(String.class)
        );
    }
}
