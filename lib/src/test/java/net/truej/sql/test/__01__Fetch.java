package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.fetch.EvenSoNullPointerException;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static net.truej.sql.fetch.Parameters.NotNull;
import static net.truej.sql.fetch.Parameters.Nullable;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __01__Fetch {

    record User(Long id, String name, @Nullable String info) { }
    record Clinic(String name, String city) { }

    @TestTemplate public void test(MainConnection cn) {

        Assertions.assertEquals(
            "Donald",
            cn.q("select name from users where id = ?", 2L).fetchOne(String.class)
        );

        // TODO: also check that warning
        Assertions.assertThrows(
            EvenSoNullPointerException.class,
            () -> cn.q("select info from users where id = ?", 1L)
                .fetchOne(NotNull, String.class)
        );

        Assertions.assertEquals(
            new User(1L, "Joe", null),
            cn.q("select id, name, info from users where id = ?", 1L)
                .fetchOneOrZero(User.class)
        );
        Assertions.assertNull(
            cn.q("select id, name, info from users where id = ?", 42L)
                .fetchOneOrZero(User.class)
        );

        Assertions.assertEquals(
            new User(1L, "Joe", null),
            cn.q("select id, name, info from users where id = ?", 1L)
                .fetchOneOrZero(User.class)
        );
        Assertions.assertNull(
            cn.q("select info from users where id = ?", 1L)
                .fetchOneOrZero(Nullable, String.class)
        );

        var allClinics = List.of("Diagnostic center", "London Heart Hospital", "Paris Neurology Hospital");

        Assertions.assertEquals(
            allClinics,
            cn.q("select name from clinic order by name").fetchList(String.class)
        );

        var clinicsAndCities = List.of(
            new Clinic("Diagnostic center", "London"),
            new Clinic("London Heart Hospital", "London"),
            new Clinic("Paris Neurology Hospital", "Paris")
        );

        Assertions.assertEquals(
            clinicsAndCities,
            cn.q("""
                select
                    cl.name,
                    ci.name as city
                from clinic cl join city ci on cl.city_id = ci.id
                order by cl.name, ci.name
                """).fetchList(Clinic.class)
        );

        try (
            var result = cn.q("select name from clinic order by name")
                .fetchStream(String.class)
        ) {
            Assertions.assertEquals(allClinics, result.toList());
        }

        var info = new ArrayList<String>() {{
            add(null); add("Do not disturb");
        }};

        Assertions.assertEquals(
            info,
            cn.q("select info from users").fetchList(Nullable, String.class)
        );

        Assertions.assertNull(
            cn.q("update users set name = 'xxx' where id = 100").fetchNone()
        );
    }
}