package com.truej.sql.showcase;

import com.truej.sql.showcase.__06__GenerateDtoG.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

public class __06__GenerateDto {

    void simple(MainDataSource ds) {
        assertEquals(
            List.of(
                new Clinic(
                    1L, "Клиника Доктора Хауса",
                    List.of("Silicon", "Valley"),
                    List.of(
                        new Doctor(1L, "Доктор Хаус"),
                        new Doctor(3L, "Доктор Айболит")
                    ),
                    List.of(
                        new Patient(1L, "Джо", List.of(
                            new Bill(1L, "USD", new BigDecimal("33.3"))
                        ))
                    )
                ),
                new Clinic(
                    2L, "Клиника Доктора Джокера",
                    List.of("sin", "hel"),
                    List.of(
                        new Doctor(1L, "Доктор Джокер")
                    ),
                    List.of(
                        new Patient(2L, "Джокер", List.of(
                            new Bill(1L, "BTC", new BigDecimal("9000"))
                        )),
                        new Patient(3L, "Чучело Мяучело", List.of())
                    )
                )
            ),
            ds.q("""
                select
                  c.id       as "        id                           ",
                  c.name     as "        name                         ",
                  a.street   as "        addresses.                   ",
                  d.id       as "Doctor  doctors  .     id            ",
                  d.name     as "        doctors  .     name          ",
                  p.id       as "Patient patients .     id            ",
                  p.name     as "        patients .     name          ",
                  b.id       as "        patients .Bill bills.id      ",
                  b.currency as "        patients .     bills.currency",
                  b.money    as "        patients .     bills.money   "
                from clinic c
                  left  join address         a on a.clinic_id  = c.id
                  left  join patient         p on p.clinic_id  = c.id
                  left  join bill            b on b.patient_id = p.id
                  left  join clinic_doctors cd on cd.clinic_id = c.id
                        join doctor          d on cd.doctor_id = d.id
                """).g.fetchList(Clinic.class)
        );
    }
}
