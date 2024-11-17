package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.truej.sql.compiler.GLangParser.*;

public class MapperGeneratorTest {
//
//    @Test void single() {
//        var out = new StatementGenerator.Out(new StringBuilder());
//
//        MapperGenerator.generate(out, new GroupedType(
//            "Bill", List.of(
//            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.Long"), "id"),
//            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "currency"),
//            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.math.BigDecimal"), "amount")
//        )), null, t -> switch (t) {
//            case "java.lang.String" -> "StringReadWrite";
//            case "java.lang.Long" -> "LongReadWrite";
//            case "java.math.BigDecimal" -> "BigDecimalReadWrite";
//            default -> throw new RuntimeException("not implemented");
//        });
//
//        Assertions.assertEquals("""
//
//                var mapped = Stream.iterate(
//                    rs, t -> {
//                        try {
//                            return t.next();
//                        } catch (SQLException e) {
//                            throw source.mapException(e);
//                        }
//                    }, t -> t
//                ).map(t -> {
//                    try {
//                        return
//                            new Bill (
//                                EvenSoNullPointerException.check(new LongReadWrite().get(rs, 1)),
//                                EvenSoNullPointerException.check(new StringReadWrite().get(rs, 2)),
//                                EvenSoNullPointerException.check(new BigDecimalReadWrite().get(rs, 3))
//                            );
//                    } catch (SQLException e) {
//                        throw source.mapException(e);
//                    }
//                })
//                ;
//                """,
//            out.buffer.toString()
//        );
//    }
//
//    @Test void grouped() {
//        var out = new StatementGenerator.Out(new StringBuilder());
//
//        MapperGenerator.generate(out, new GroupedType(
//            "Clinic", List.of(
//            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.Long"), "id"),
//            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "name"),
//            new Field(
//                new GroupedType("List<java.lang.String>", List.of(
//                    new Field(new ScalarType(NullMode.EXACTLY_NULLABLE, "java.lang.String"), null)
//                )),
//                "addresses"
//            ),
//            new Field(
//                new GroupedType("Doctor", List.of(
//                    new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.Long"), "id"),
//                    new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "name")
//                )),
//                "doctors"
//            ),
//            new Field(
//                new GroupedType("Patient", List.of(
//                    new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.Long"), "id"),
//                    new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "name"),
//                    new Field(
//                        new GroupedType("Bill", List.of(
//                            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.Long"), "id"),
//                            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "currency"),
//                            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.math.BigDecimal"), "money")
//                        )),
//                        "bills"
//                    )
//                )),
//                "patients"
//            )
//        )), null, t -> switch (t) {
//            case "java.lang.String" -> "StringReadWrite";
//            case "java.lang.Long" -> "LongReadWrite";
//            case "java.math.BigDecimal" -> "BigDecimalReadWrite";
//            default -> throw new RuntimeException("not implemented");
//        });
//
//        Assertions.assertEquals("""
//                record Row(
//                    java.lang.Long c1,
//                    java.lang.String c2,
//                    java.lang.String c3,
//                    java.lang.Long c4,
//                    java.lang.String c5,
//                    java.lang.Long c6,
//                    java.lang.String c7,
//                    java.lang.Long c8,
//                    java.lang.String c9,
//                    java.math.BigDecimal c10
//                ) {}
//                record G1(
//                    java.lang.Long c1,
//                    java.lang.String c2
//                ) {}
//                record G2(
//                    java.lang.Long c6,
//                    java.lang.String c7
//                ) {}
//
//
//                var mapped = Stream.iterate(
//                    rs, t -> {
//                        try {
//                            return t.next();
//                        } catch (SQLException e) {
//                            throw source.mapException(e);
//                        }
//                    }, t -> t
//                ).map(t -> {
//                    try {
//                        return
//                            new Row (
//                                new LongReadWrite().get(rs, 1),
//                                new StringReadWrite().get(rs, 2),
//                                new StringReadWrite().get(rs, 3),
//                                new LongReadWrite().get(rs, 4),
//                                new StringReadWrite().get(rs, 5),
//                                new LongReadWrite().get(rs, 6),
//                                new StringReadWrite().get(rs, 7),
//                                new LongReadWrite().get(rs, 8),
//                                new StringReadWrite().get(rs, 9),
//                                new BigDecimalReadWrite().get(rs, 10)
//                            );
//                    } catch (SQLException e) {
//                        throw source.mapException(e);
//                    }
//                })
//                .collect(
//                    java.util.stream.Collectors.groupingBy(
//                        r -> new G1(
//                            r.c1,
//                            r.c2
//                        ), java.util.LinkedHashMap::new, Collectors.toList()
//                    )
//                ).entrySet().stream()
//                .filter(g1 ->
//                    java.util.Objects.nonNull(g1.getKey().c1) ||
//                    java.util.Objects.nonNull(g1.getKey().c2)
//                ).map(g1 ->
//                    new Clinic(
//                        EvenSoNullPointerException.check(g1.getKey().c1),
//                        EvenSoNullPointerException.check(g1.getKey().c2),
//                        g1.getValue().stream().filter(r ->
//                            java.util.Objects.nonNull(r.c3)
//                        ).map(r ->
//                            r.c3
//                        ).distinct().toList(),
//                        g1.getValue().stream().filter(r ->
//                            java.util.Objects.nonNull(r.c4) ||
//                            java.util.Objects.nonNull(r.c5)
//                        ).map(r ->
//                            new Doctor(
//                                EvenSoNullPointerException.check(r.c4),
//                                EvenSoNullPointerException.check(r.c5)
//                            )
//                        ).distinct().toList(),
//                        g1.getValue().stream().collect(
//                            java.util.stream.Collectors.groupingBy(
//                                r -> new G2(
//                                    r.c6,
//                                    r.c7
//                                ), java.util.LinkedHashMap::new, Collectors.toList()
//                            )
//                        ).entrySet().stream()
//                        .filter(g2 ->
//                            java.util.Objects.nonNull(g2.getKey().c6) ||
//                            java.util.Objects.nonNull(g2.getKey().c7)
//                        ).map(g2 ->
//                            new Patient(
//                                EvenSoNullPointerException.check(g2.getKey().c6),
//                                EvenSoNullPointerException.check(g2.getKey().c7),
//                                g2.getValue().stream().filter(r ->
//                                    java.util.Objects.nonNull(r.c8) ||
//                                    java.util.Objects.nonNull(r.c9) ||
//                                    java.util.Objects.nonNull(r.c10)
//                                ).map(r ->
//                                    new Bill(
//                                        EvenSoNullPointerException.check(r.c8),
//                                        EvenSoNullPointerException.check(r.c9),
//                                        EvenSoNullPointerException.check(r.c10)
//                                    )
//                                ).distinct().toList()
//                            )
//                        ).toList()
//                    )
//                );
//                """,
//            out.buffer.toString()
//        );
//    }
}
