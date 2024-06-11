package com.truej.sql.compiler;

import com.truej.sql.v3.compiler.MapperGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.truej.sql.v3.compiler.GLangParser.*;

public class MapperGeneratorTest {

    @Test void single() {
        Assertions.assertEquals(
            """
                static java.util.stream.Stream<Bill> doTheMapping(java.sql.ResultSet rs) {   
                    return java.util.stream.Stream.iterate(
                        rs, t -> {
                            try {
                                return t.next();
                            } catch (java.sql.SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }, t -> t
                    ).map(t -> {
                        try {
                            return new Bill(
                                t.getObject(1, Long.class),
                                t.getString(2),
                                t.getBigDecimal(3)
                            );
                        } catch(java.sql.SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }
                """,
            MapperGenerator.generate("Bill", List.of(
                new Field(new ScalarType("java.lang.Long"), "id"),
                new Field(new ScalarType("java.lang.String"), "currency"),
                new Field(new ScalarType("java.math.BigDecimal"), "amount")
            ), (t, i) -> switch (t) {
                case "java.lang.String" -> STR."t.getString(\{i})";
                case "java.lang.Long" -> STR."t.getObject(\{i}, Long.class)";
                case "java.math.BigDecimal" -> STR."t.getBigDecimal(\{i})";
                default -> throw new RuntimeException("not implemented");
            })
        );
    }

    @Test void grouped() {
        Assertions.assertEquals(
            """
                static java.util.stream.Stream<Clinic> doTheMapping(java.sql.ResultSet rs) {
                    record Row(
                        java.lang.Long c1,
                        java.lang.String c2,
                        java.lang.String c3,
                        java.lang.Long c4,
                        java.lang.String c5,
                        java.lang.Long c6,
                        java.lang.String c7,
                        java.lang.Long c8,
                        java.lang.String c9,
                        java.math.BigDecimal c10
                    ) {}
                    record G1(
                        java.lang.Long c1,
                        java.lang.String c2
                    ) {}
                    record G2(
                        java.lang.Long c6,
                        java.lang.String c7
                    ) {}
                                
                    return java.util.stream.Stream.iterate(
                        rs, t -> {
                            try {
                                return t.next();
                            } catch (java.sql.SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }, t -> t
                    ).map(t -> {
                        try {
                            return new Row(
                                t.getObject(1, Long.class),
                                t.getString(2),
                                t.getString(3),
                                t.getObject(4, Long.class),
                                t.getString(5),
                                t.getObject(6, Long.class),
                                t.getString(7),
                                t.getObject(8, Long.class),
                                t.getString(9),
                                t.getBigDecimal(10)
                            );
                        } catch(java.sql.SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).collect(
                        java.util.stream.Collectors.groupingBy(
                            r -> new G1(
                                r.c1,
                                r.c2
                            ), java.util.LinkedHashMap::new, Collectors.toList()
                        )
                    ).entrySet().stream()
                    .filter(g1 ->
                        java.util.Objects.nonNull(g1.getKey().c1) &&
                        java.util.Objects.nonNull(g1.getKey().c2)\s
                    ).map(g1 ->
                        new Clinic(
                            g1.getKey().c1,
                            g1.getKey().c2,
                            g1.getValue().stream().filter(r ->
                                java.util.Objects.nonNull(r.c3)\s
                            ).map(r ->
                                    r.c3
                            ).distinct().toList(),
                            g1.getValue().stream().filter(r ->
                                java.util.Objects.nonNull(r.c4) &&
                                java.util.Objects.nonNull(r.c5)\s
                            ).map(r ->
                                new Doctor(
                                    r.c4,
                                    r.c5
                                )
                            ).distinct().toList(),
                            g1.getValue().stream().collect(
                                java.util.stream.Collectors.groupingBy(
                                    r -> new G2(
                                        r.c6,
                                        r.c7
                                    ), java.util.LinkedHashMap::new, Collectors.toList()
                                )
                            ).entrySet().stream()
                            .filter(g2 ->
                                java.util.Objects.nonNull(g2.getKey().c6) &&
                                java.util.Objects.nonNull(g2.getKey().c7)\s
                            ).map(g2 ->
                                new Patient(
                                    g2.getKey().c6,
                                    g2.getKey().c7,
                                    g2.getValue().stream().filter(r ->
                                        java.util.Objects.nonNull(r.c8) &&
                                        java.util.Objects.nonNull(r.c9) &&
                                        java.util.Objects.nonNull(r.c10)\s
                                    ).map(r ->
                                        new Bill(
                                            r.c8,
                                            r.c9,
                                            r.c10
                                        )
                                    ).distinct().toList()
                                )
                            ).toList()
                        )
                    );
                }
                """,
            MapperGenerator.generate("Clinic", List.of(
                new Field(new ScalarType("java.lang.Long"), "id"),
                new Field(new ScalarType("java.lang.String"), "name"),
                new Field(
                    new AggregatedType(null, List.of(
                        new Field(new ScalarType("java.lang.String"), null)
                    )),
                    "addresses"
                ),
                new Field(
                    new AggregatedType("Doctor", List.of(
                        new Field(new ScalarType("java.lang.Long"), "id"),
                        new Field(new ScalarType("java.lang.String"), "name")
                    )),
                    "doctors"
                ),
                new Field(
                    new AggregatedType("Patient", List.of(
                        new Field(new ScalarType("java.lang.Long"), "id"),
                        new Field(new ScalarType("java.lang.String"), "name"),
                        new Field(
                            new AggregatedType("Bill", List.of(
                                new Field(new ScalarType("java.lang.Long"), "id"),
                                new Field(new ScalarType("java.lang.String"), "currency"),
                                new Field(new ScalarType("java.math.BigDecimal"), "money")
                            )),
                            "bills"
                        )
                    )),
                    "patients"
                )
            ), (t, i) -> switch (t) {
                case "java.lang.String" -> STR."t.getString(\{i})";
                case "java.lang.Long" -> STR."t.getObject(\{i}, Long.class)";
                case "java.math.BigDecimal" -> STR."t.getBigDecimal(\{i})";
                default -> throw new RuntimeException("not implemented");
            })
        );
    }
}
