package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import com.truej.sql.showcase.__06__GenerateDtoG.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.truej.sql.showcase.__06__GenerateDto.Dto.*;

import java.math.BigDecimal;
import java.util.List;

public class __06__GenerateDto {
    @Test void simple(MainDataSource ds) {
        assertEquals(
            ds."select id, name, email from users where id = \{ 42 }"
                .g.fetchOne(User.class)
            , new User(42, "Joe", "example@email.com")
        );
    }

    @Test void grouped(MainDataSource ds) {
        assertEquals(
            ds."""
                    select
                        c.id        as "id                       ",
                        c.name      as "name                     ",
                        user.id     as ".patients<>.id           ",
                        user.name   as ".patients<>.name         ",
                        bank.id     as ".patients<>.banks[].id   ",
                        bank.money  as ".patients<>.banks[].money",
                        doctor.id   as ".doctors[].id            ",
                        doctor.name as ".doctors[].name          "
                    from clinics c
                    inner join doctors d on d.clinic_id = c.id
                    inner join users   u on u.clinic_id = c.id
                    inner join banks   b on b.user_id   = u.id
                """.g.fetchOne(Clinic.class),
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
