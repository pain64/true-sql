package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.fetch.EvenSoNullPointerException;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static net.truej.sql.source.Parameters.NotNull;
import static net.truej.sql.source.Parameters.Nullable;


@ExtendWith(TrueSqlTests.class)
@TrueSql public class __01__Fetch {
    record User(Long id, String name, @Nullable String info) {}
    record Clinic(String name, String city) {}
    // FIXME: rename to CityClinics
    record CitiesClinics(String name, List<String> clinics) {}
    @Test public void test(MainConnection cn) {

        Assertions.assertEquals(
                "Donald",
                cn.q("select name from user where id = ?", 2).fetchOne(String.class)
        );

        // TODO: also check that warning
        Assertions.assertThrows(
            EvenSoNullPointerException.class,
            () -> cn.q("select info from user where id = ?", 1)
                .fetchOne(NotNull, String.class)
        );

//        кстати сюда нельзя вставить юзера
//        record User(long id, String name, @Nullable String info) {}
//        FIXME: type mismatch for column 1 (for field `id`). Expected long but has java.lang.Long
//        Assertions.assertEquals(
//                new User(1L,"Joe", null),
//                cn.q("select id, name, info from user where id = ?", 1).fetchOneOrZero(User.class)
//        );

        Assertions.assertEquals(
                new User(1L,"Joe", null),
                cn.q("select id, name, info from user where id = ?", 1)
                        .fetchOneOrZero(User.class)
        );
        Assertions.assertNull(
                cn.q("select id, name, info from user where id = ?", 42)
                        .fetchOneOrZero(User.class)
        );

        Assertions.assertEquals(
                new User(1L,"Joe", null),
                cn.q("select id, name, info from user where id = ?", 1)
                        .fetchOneOrZero(User.class)
        );
        Assertions.assertNull(
                cn.q("select info from user where id = ?", 1)
                        .fetchOneOrZero(Nullable, String.class)
        );

        var allClinics = List.of("Paris Neurology Hospital", "London Heart Hospital", "Diagnostic center");

        Assertions.assertEquals(
                allClinics,
                cn.q("select name from clinic").fetchList(String.class)
        );

        Assertions.assertEquals(
                allClinics,
                cn.q("select name from clinic").fetchStream(String.class).toList()
        );

        var clinicsAndCities = List.of(
                new Clinic("Paris Neurology Hospital", "Paris"),
                new Clinic("London Heart Hospital", "London"),
                new Clinic("Diagnostic center", "London")
        );

        Assertions.assertEquals(
                clinicsAndCities,
                cn.q("""
                select
                    cl.name,
                    ci.name as city
                from clinic cl join city ci on cl.city_id = ci.id
                """).fetchList(Clinic.class)
        );

        Assertions.assertEquals(
                clinicsAndCities,
                cn.q("""
                select
                    cl.name,
                    ci.name as city
                from clinic cl join city ci on cl.city_id = ci.id
                """).fetchStream(Clinic.class).toList()
        );


        var info = new ArrayList<String>() {{
            add(null); add("Do not disturb");
        }};

        Assertions.assertEquals(
                info,
                cn.q("select info from user").fetchList(Nullable, String.class)
        );

        //FIXME Не подсказывает трайкетч?
        Assertions.assertEquals(
                info,
                cn.q("select info from user").fetchStream(Nullable, String.class).toList()
        );

        var citiesClinics = List.of(
                new CitiesClinics("Paris", List.of("Paris Neurology Hospital")),
                new CitiesClinics("London", List.of("London Heart Hospital", "Diagnostic center"))
        );

        Assertions.assertEquals(
                citiesClinics,
                cn.q("""
                select
                    ci.name as city,
                    cl.name
                from clinic cl join city ci on cl.city_id = ci.id
                """).fetchList(CitiesClinics.class)
        );


        Assertions.assertNull(
                cn.q("insert into user(id, name, info) values(?, ?, ?)",3L, "Mike", "Strong left hook")
                        .fetchNone()
        );
        //FIXME: cannot find type binding for null
//        Assertions.assertNull(
//                cn.q("insert into user(id, name, info) values(?, ?, ?)",4L, "Ali", null)
//                        .fetchNone()
//        );

        //cn.q()

    }
}

