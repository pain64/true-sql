package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;

//@Disabled
public class StatementGeneratorTest {
//    @Test void batch() {
//        Assertions.assertEquals(
//            """
//
//                public static class User {
//                    public final java.lang.Long id;
//                    @Nullable public final java.lang.String name;
//
//                    public User(
//                        java.lang.Long id,
//                        java.lang.String name
//                    ) {
//                        this.id = id;
//                        this.name = name;
//                    }
//
//                    @Override public boolean equals(Object other) {
//                        return this == other || (
//                            other instanceof User o &&
//                            java.util.Objects.equals(this.id, o.id) &&
//                            java.util.Objects.equals(this.name, o.name)
//                        );
//                    }
//
//                    @Override public int hashCode() {
//                        int h = 1;
//                        h = h * 59 + java.util.Objects.hashCode(this.id);
//                        h = h * 59 + java.util.Objects.hashCode(this.name);
//                        return h;
//                    }
//                }
//                public static <B, P1, E1 extends Exception>
//                List<User> fetchList__line12__(
//                    List<B> batch,
//                    Function<B, P1> pe1,
//                    TypeReadWrite<P1> prw1,
//                    ConnectionW source
//                ) throws E1 {
//                    var query = ""\"
//                        select id, name from users where id = ?
//                        ""\";
//                   \s
//                    try {
//                        var connection = source.w();
//                        try (var stmt = connection.prepareStatement(query)) {
//                       \s
//                            for (var element : batch) {
//                                var p1 = pe1.get(element);
//                                prw1.set(stmt, 1, p1);
//                                stmt.addBatch();
//                            }
//                           \s
//                            var updateCount = stmt.executeLargeBatch();
//                           \s
//                            var rs = stmt.getResultSet();
//                           \s
//                           \s
//                            var mapped = Stream.iterate(
//                                rs, t -> {
//                                    try {
//                                        return t.next();
//                                    } catch (SQLException e) {
//                                        throw source.mapException(e);
//                                    }
//                                }, t -> t
//                            ).map(t -> {
//                                try {
//                                    return
//                                        new User (
//                                            EvenSoNullPointerException.check(new LongReadWrite().get(rs, 1)),
//                                            new StringReadWrite().get(rs, 2)
//                                        );
//                                } catch (SQLException e) {
//                                    throw source.mapException(e);
//                                }
//                            })
//                            ;
//                            return mapped.toList();
//                       \s
//                        }
//                    } catch (SQLException e) {
//                        throw source.mapException(e);
//                    }
//                }
//                """,
//            generate(
//                t -> switch (t) {
//                    case "java.lang.String" -> "StringReadWrite";
//                    case "java.lang.Long" -> "LongReadWrite";
//                    default -> throw new RuntimeException("not implemented");
//                },
//                12,
//                SourceMode.CONNECTION,
//                new BatchedQuery(null, null, List.of(
//                    new InvocationsFinder.TextPart("select id, name from users where id = "),
//                    new InvocationsFinder.InParameter(null),
//                    new InvocationsFinder.TextPart("")
//                )),
//                new AsDefault(),
//                new FetchList(
//                    new GroupedType(
//                        "User", List.of(
//                        new Field(new ScalarType(NullMode.DEFAULT_NOT_NULL, "java.lang.Long"), "id"),
//                        new Field(new ScalarType(NullMode.EXACTLY_NULLABLE, "java.lang.String"), "name")
//                    ))
//                ),
//                true,
//                false
//            )
//        );
//    }
//
//    @Test void singleUnfold() {
//        Assertions.assertEquals(
//            """
//                public static <P1, P2A1, P2A2>
//                UpdateResult<Long, List<java.lang.String>> fetchList__line12__(
//                    P1 p1,
//                    TypeReadWrite<P1> prw1,
//                    List<Pair<P2A1, P2A2>> p2,
//                    TypeReadWrite<P2A1> prw2a1,
//                    TypeReadWrite<P2A2> prw2a2,
//                    DataSourceW source
//                )  {
//                    var buffer = new StringBuilder();
//                   \s
//                    buffer.append(""\"
//                        select name from users where id = ""\");
//                    buffer.append(" ? ");
//                    buffer.append(""\"
//                        and (name, age) in (""\");
//                    for (var i = 0; i < p2.size(); i++) {
//                        buffer.append(" (?, ?) ");
//                        if (i != p2.size() - 1)
//                            buffer.append(", ");
//                    }
//                    buffer.append(""\"
//                        )""\");
//                   \s
//                    var query = buffer.toString();
//                   \s
//                    try (var connection = source.w().getConnection()) {
//                        try (var stmt = connection.prepareStatement(query)) {
//                       \s
//                            var n = 0;
//                            prw1.set(stmt, ++n, p1);
//                           \s
//                            for (var element : p2) {
//                                prw2a1.set(stmt, ++n, element.a1());
//                                prw2a2.set(stmt, ++n, element.a2());
//                            }
//                           \s
//                            stmt.execute();
//                           \s
//                            var rs = stmt.getResultSet();
//                           \s
//                           \s
//                            var mapped = Stream.iterate(
//                                rs, t -> {
//                                    try {
//                                        return t.next();
//                                    } catch (SQLException e) {
//                                        throw source.mapException(e);
//                                    }
//                                }, t -> t
//                            ).map(t -> {
//                                try {
//                                    return
//                                        EvenSoNullPointerException.check(new StringReadWrite().get(rs, 1));
//                                } catch (SQLException e) {
//                                    throw source.mapException(e);
//                                }
//                            })
//                            ;
//                            return new UpdateResult<>(stmt.getLargeUpdateCount(), mapped.toList());
//                       \s
//                        }
//                    } catch (SQLException e) {
//                        throw source.mapException(e);
//                    }
//                }
//                """,
//            generate(
//                t -> switch (t) {
//                    case "java.lang.String" -> "StringReadWrite";
//                    default -> throw new RuntimeException("not implemented");
//                },
//                12,
//                SourceMode.DATASOURCE,
//                new SingleQuery(List.of(
//                    new InvocationsFinder.TextPart("select name from users where id = "),
//                    new InvocationsFinder.InParameter(null),
//                    new InvocationsFinder.TextPart("and (name, age) in ("),
//                    new InvocationsFinder.UnfoldParameter(null, null),
//                    new InvocationsFinder.TextPart(")")
//                )),
//                new AsDefault(),
//                new FetchList(new ScalarType(NullMode.DEFAULT_NOT_NULL, "java.lang.String")),
//                false,
//                true
//            )
//        );
//    }
}
