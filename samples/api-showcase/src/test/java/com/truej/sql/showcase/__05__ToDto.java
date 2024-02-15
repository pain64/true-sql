package com.truej.sql.showcase;

import com.truej.sql.v3.Group;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __05__ToDto {
    record User(long id, String name, String email) { }

    @Test void simple(DataSource ds) {
        assertEquals(
            Stmt. "select id, name, email from users where id = \{ 42 }"
                .fetchOne(ds, m(User.class))
            , new User(42, "Joe", "example@email.com")
        );
    }

    // TODO: пример OK?
    // FIXME: is @Group OK?

    record Bank(long id, BigDecimal money) { }
    record Patient(long id, String name, @Group List<Bank> banks) { }
    record Doctor(long id, String name) { }
    record Clinic(
        long id, String name,
        @Group List<Patient> patients,
        @Group List<Doctor> doctors
    ) { }

    @Test void grouped(DataSource ds) {
        assertEquals(
            Stmt."""
                    select
                        c.id        as "id                    ",
                        c.name      as "name                  ",
                        user.id     as ".patients.id          ",
                        user.name   as ".patients.name        ",
                        bank.id     as ".patients.banks.id    ",
                        bank.money  as ".patients.banks.money ",
                        doctor.id   as ".doctors.id           ",
                        doctor.name as ".doctors.name         "
                    from clinics c
                    inner join doctors d on d.clinic_id = c.id
                    inner join users   u on u.clinic_id = c.id
                    inner join banks   b on b.user_id   = u.id
                """.fetchOne(ds, m(Clinic.class)),
            new Clinic(
                1L, "Pet clinic",
                List.of(
                    new Patient(1, "Joe", List.of(
                        new Bank(1L, new BigDecimal(10)),
                        new Bank(2L, new BigDecimal(200))
                    ))
                ),
                List.of(
                    new Doctor(1L, "AiBolit"),
                    new Doctor(2L, "NeBolit")
                )
            )
        );
    }
}
