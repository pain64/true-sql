package com.truej.sql.showcase;

import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.truej.sql.showcase.__06__GenerateDtoG.*;

import static com.truej.sql.showcase.__06__GenerateDto.F.B;
import static com.truej.sql.showcase.__06__GenerateDto.Nulls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.truej.sql.showcase.__06__GenerateDto.Dto.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class __06__GenerateDto {

    enum Nulls {Nullable}

    class AAA {
        static <T> @Nullable T fetchOne(Nulls mayNull, Class<T> tClass) {
            return null;
        }
        static <T> @NotNull T fetchOne(Class<T> tClass) {
            return null;
        }
        static <T> @NotNull T fetchOne() {
            return null;
        }
    }

    class F<T> implements StringTemplate.Processor<T, RuntimeException> {
        public F(DataSourceW ds) {

        }

        public F(ConnectionW ds) {

        }

        @Override public T process(StringTemplate stringTemplate) throws RuntimeException {
            return null;
        }

        public static class BatchQuery { }
        public static final StringTemplate.Processor<BatchQuery, RuntimeException> B = null;

        public <P> T batched(List<P> parameters, Function<P, BatchQuery> builder) {
            return null;
        }
    }

    static class OutParameters<T> {
        T v;
    }

    static class UpdateCount<T> {

    }

    @Target(ElementType.TYPE_USE) @interface G { }

    void simple(MainDataSource ds) {
//        var x = AAA.<@Nullable String>fetchOne();
//        x.trim();
//
//        var xx = AAA.fetchOne(Nullable, String.class);
//        xx.trim();
//
//        var xxx = AAA.fetchOne(String.class);
//        var _ = xxx.trim();
//
//        var customerId = ds
//            ."select id from customer where name = 'Josh'"
//            .fetchOne(Long.class);
//
//        assertEquals(
//            new User(42, "Joe", "example@email.com"),
//            ds."select id, name, email from users where id = \{42}"
//                .g.fetchOne(User.class)
//        );
//
//        var xcc0 = new F<OutParameters<String>>(ds)
//            ."select id, name, email from users where id = \{42}".v;
//
//        var z = ds.withConnection(
//            cn -> new F<@Nullable String>(
//                cn, new AsGeneratedKeys("id")
//            )."""
//               select id, name, email from users where id = \{42}
//               long query"""
//        );
//
//        var _ = z.trim();
//
//        var xcc1 = new F<@Nullable String>(ds)
//            //     .asCallable()
//            //   .withUpdateCount()
//            ."select id, name, email from users where id = \{42}";
//
//        var xcc2 = new F<Stream<@G User>>(
//            ds, new AsGeneratedKeys("id")
//        )."select id, name, email from users where id = \{42}";
//
//        var xcc4 = new F<Stream<@G User>>(ds)."""
//            select id, name, email
//            from users where id = \{42}""";

//        var xcc3 = new F<Void>(ds).withUpdateCount().batched(
//            List.of(1, 2, 3), id -> B."delete from users where id = \{id}"
//        );

        // fetchOne
        // fetchOneOrZero
        // fetchOneOfNullable(String.class)
        // fetchOneOfNullableOrZero(String.class)

        // fetchList
        // fetchListOfNullable

        // fetchStream
        // fetchStreamOfNullable

        // one
        // oneOfNullable(String.class)

        // oneOrZero(String.class)
        // oneOrZeroOfNullable(String.class)

        // list
        // listOfNullable

        // stream
        // streamOfNullable

//        assertEquals(
//            new User(42, "Joe", "example@email.com"),
//            ds."select id, name, email from users where id = \{42}"
//                .g.fetchOne(User.class)
//                .g.fetchOne(nullable(String.class))
//            // .g.fetch(new R<Void>())
//        );
    }

//    create table clinic          (id         bigint, "name"     varchar(64)                                              );
//    create table clinic_addresses(clinic_id  bigint, street     varchar(128)                                             );
//    create table patient         (id         bigint, clinic_id  bigint      , "name"  varchar(64)                        );
//    create table patient_bills   (id         bigint, patient_id bigint      , currency  varchar(32), money numeric(15, 2));
//    create table doctor          (id         bigint, "name"     varchar(64)                                              );
//    create table clinic_doctors  (clinic_id  bigint, doctor_id  bigint                                                   );
//
//
//
//    drop table clinic;
//    drop table clinic_addresses;
//    drop table patient_bills;
//    drop table patient;
//    drop table doctor;
//    drop table clinic_doctors;
//
//    insert into clinic values (1, 'Клиника Доктора Хауса');
//    insert into clinic values (2, 'Клиника Доктора Джокера');
//    select *  from clinic;
//
//    insert into clinic_addresses values(1, 'Silicon');
//    insert into clinic_addresses values(1, 'Valley');
//    insert into clinic_addresses values(2, 'sin');
//    insert into clinic_addresses values(2, 'hel');
//    select * from clinic_addresses;
//
//    insert into patient values(1, 1, 'Джо');
//    insert into patient values(2, 2, 'Джокер');
//    insert into patient values(3, 2, 'Чучело Мяучело');
//    select * from patient;
//
//    insert into patient_bills values(1, 1, 'USD', 33.3);
//    insert into patient_bills values(2, 2, 'BTC', 9000);
//    select * from patient_bills;
//
//    insert into doctor values(1, 'Доктор Хаус');
//    insert into doctor values(2, 'Доктор Джокер');
//    insert into doctor values(3, 'Доктор Айболит');
//    select * from doctor;
//
//    insert into clinic_doctors values (1, 1);
//    insert into clinic_doctors values (1, 3);
//    insert into clinic_doctors values (2, 2);
//    select * from clinic_doctors;


    void grouped(MainDataSource ds) throws SQLException {
        var cl = int.class;

        record Row(
            long c1,       // clinic id
            String c2,     // clinic name
            @Nullable String c3,     // clinic address
            @Nullable Long c4,       // doctor id
            @Nullable String c5,     // doctor name
            @Nullable Long c6,       // patient id
            @Nullable String c7,     // patient name
            @Nullable Long c8,       // bill id
            @Nullable String c9,     // bill currency
            @Nullable BigDecimal c10 // bill money
        ) { }
        record G1(Long c1, String c2) { }
        record G2(Long c6, String c7) { }

        ResultSet initialResultSet = null;

        var mapped = Stream.iterate(
            initialResultSet, t -> {
                try {
                    return t.next();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, rs -> rs
        ).map(rs -> {
            try {
                return new Row(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getLong(4),
                    rs.getString(5),
                    rs.getLong(6),
                    rs.getString(7),
                    rs.getLong(8),
                    rs.getString(9),
                    rs.getBigDecimal(10)
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).collect(
            Collectors.groupingBy(
                r -> new G1(r.c1, r.c2),
                LinkedHashMap::new, Collectors.toList()
            )
        ).entrySet().stream().map(a1 ->
            new Clinic(
                a1.getKey().c1,
                a1.getKey().c2,

                a1.getValue().stream().map(a2 ->
                    a2.c3
                ).filter(Objects::nonNull).distinct().toList(),

                a1.getValue().stream().map(r ->
                        new Doctor(r.c4, r.c5)
                    )
                    .filter(r -> !(r.id() == null && r.name() == null))
                    .distinct().toList(),

                a1.getValue().stream().collect(
                        Collectors.groupingBy(
                            r -> new G2(r.c6, r.c7),
                            LinkedHashMap::new, Collectors.toList()
                        )
                    ).entrySet().stream()
                    .filter(a2 -> !(a2.getKey().c6 == null && a2.getKey().c7 == null))
                    .map(a2 ->
                        new Patient(
                            a2.getKey().c6,
                            a2.getKey().c7,

                            a2.getValue().stream().map(r ->
                                    new Bill(r.c8, r.c9, r.c10)
                                )
                                .filter(r -> !(r.id() == null && r.currency() == null && r.money() == null))
                                .distinct().toList()
                        )
                    ).toList()
            )
        ).toList();

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
            ds."""
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
                """.g.fetchList(Clinic.class)
        );
    }
}
