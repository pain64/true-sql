package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __04__ToDto {

    public record City(Long id, String name) { }
    public record CityClinics(String city, List<String> clinics) { }

    public record User(String name, @Nullable String info, @NotNull BigDecimal amount) { }
    public record Report(String city, List<String> clinics, List<User> users) { }
    public record User2(@Nullable String name, @Nullable String info, @Nullable BigDecimal amount) { }
    public record Report2(String city, List<String> clinics, List<User2> users) { }

    @TestTemplate public void test(MainConnection cn) {

        Assertions.assertEquals(
            new City(1L, "London"),
            cn.q("select * from city where id = 1").fetchOne(City.class)
        );

        var citiesClinics = List.of(
            new CityClinics("Paris", List.of("Paris Neurology Hospital")),
            new CityClinics("London", List.of("London Heart Hospital", "Diagnostic center"))
        );

        Assertions.assertEquals(
            citiesClinics,
            cn.q("""
                select
                    ci.name as city,
                    cl.name
                from clinic cl join city ci on cl.city_id = ci.id"""
            ).fetchList(CityClinics.class)
        );

        var report = List.of(
            new Report("London",
                List.of("London Heart Hospital"),
                List.of(new User("Joe", null, new BigDecimal("3000.75")))
            ),
            new Report("Paris",
                List.of("Paris Neurology Hospital"),
                List.of(new User("Donald", "Do not disturb", new BigDecimal("12500.87")))
            )
        );

        Assertions.assertEquals(
            report,
            cn.q("""
                select
                    ci.name as city,
                    cl.name as clinic,
                    u.name as "user",
                    u.info as info,
                    sum(b.amount) as amount
                from city ci
                    join clinic cl on ci.id = cl.city_id
                    join clinic_users clu on clu.clinic_id = cl.id
                    join users u on clu.user_id = u.id
                    join user_bills ub on ub.user_id = u.id
                    join bill b on b.id = ub.bill_id
                group by ci.name, cl.name, u.name, u.info
                order by city, clinic, user, info, amount"""
            ).fetchList(Report.class)
        );

        Assertions.assertEquals(
            List.of(
                new Report2("London",
                    List.of("Diagnostic center", "London Heart Hospital"),
                    List.of(new User2("Joe", null, new BigDecimal("3000.75")))
                ),
                new Report2("Paris",
                    List.of("Paris Neurology Hospital"),
                    List.of(new User2("Donald", "Do not disturb", new BigDecimal("12500.87")))
                )
            ),
            cn.q("""
                select
                    ci.name as city,
                    cl.name as clinic,
                    u.name as "user",
                    u.info as info,
                    sum(b.amount) as amount
                from city ci
                    join clinic cl on ci.id = cl.city_id
                    left join clinic_users clu on clu.clinic_id = cl.id
                    left join users u on clu.user_id = u.id
                    left join user_bills ub on ub.user_id = u.id
                    left join bill b on b.id = ub.bill_id
                group by ci.name, cl.name, u.name, u.info
                order by ci.name, cl.name, u.name, u.info"""
            ).fetchList(Report2.class)
        );
    }
}
