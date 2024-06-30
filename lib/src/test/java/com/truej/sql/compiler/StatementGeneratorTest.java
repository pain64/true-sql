package com.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.StatementGenerator.*;
import static com.truej.sql.v3.compiler.TrueSqlAnnotationProcessor.*;

public class StatementGeneratorTest {
    @Test void batch() {
        Assertions.assertEquals(
            """
                
                class User {
                    public final java.lang.Long id;
                    public final java.lang.String name;
                                
                    public User(
                        java.lang.Long id,
                        java.lang.String name
                    ) {
                        this.id = id;
                        this.name = name;
                    }
                                
                    @Override public boolean equals(Object other) {
                        return this == other || (
                            other instanceof User o &&
                            java.util.Objects.equals(this.id, o.id) &&
                            java.util.Objects.equals(this.name, o.name)
                        );
                    }
                                
                    @Override public int hashCode() {
                        int h = 1;
                        h = h * 59 + java.util.Objects.hashCode(this.id);
                        h = h * 59 + java.util.Objects.hashCode(this.name);
                        return h;
                    }
                }
                <B, P1, E1 extends Exception>
                List<User> fetchList__line12__(
                    List<B> batch,
                    ParameterExtractor<B, P1, E1> pe1,
                    TypeReadWrite<P1> prw1,
                    ConnectionW cn
                ) throws E1 {
                    var query = ""\"
                        select id, name from users where id = ?
                        ""\";
                   \s
                    try {
                        var connection = source.w();
                        try (var stmt = connection.prepareStatement(query)) {
                       \s
                            for (var element : batch) {
                                var p1 = pe1.get(element);
                                prw1.set(stmt, 1, p1);
                                stmt.addBatch();
                            }
                           \s
                            var updateCount = stmt.executeLargeBatch();
                           \s
                            var rs = stmt.getResultSet()
                           \s
                           \s
                            var mapped = Stream.iterate(
                                rs, t -> {
                                    try {
                                        return t.next();
                                    } catch (SQLException e) {
                                        throw source.mapException(e);
                                    }
                                }, t -> t
                            ).map(t -> {
                                try {
                                    return
                                        new User (
                                            Objects.requireNonNull(new LongReadWrite().get(rs, 1)),
                                            new StringReadWrite().get(rs, 2)
                                        );
                                } catch (SQLException e) {
                                    throw source.mapException(e);
                                }
                            })
                            ;
                            return mapped.toList();
                       \s
                        }
                    } catch (SQLException e) {
                        throw source.mapException(e);
                    }
                }
                """,
            generate(
                t -> switch (t) {
                    case "java.lang.String" -> "StringReadWrite";
                    case "java.lang.Long" -> "LongReadWrite";
                    default -> throw new RuntimeException("not implemented");
                },
                12,
                SourceMode.CONNECTION,
                new BatchedQuery(List.of(
                    new TextPart("select id, name from users where id = "),
                    new SimpleParameter(null),
                    new TextPart("")
                )),
                new AsDefault(),
                new FetchList(
                    new AggregatedType(
                        "User", List.of(
                        new Field(new ScalarType(NullMode.DEFAULT_NOT_NULL, "java.lang.Long"), "id"),
                        new Field(new ScalarType(NullMode.EXACTLY_NULLABLE, "java.lang.String"), "name")
                    ))
                ),
                true,
                false
            )
        );
    }

    interface ParameterExtractor<B, P, E extends Exception> {
        P get(B element) throws E;
    }

    @Test void singleUnfold() {
        Assertions.assertEquals(
            """
                <P1, P2A1, P2A2>
                UpdateResult<Long, List<java.lang.String>> fetchList__line12__(
                    P1 p1,
                    TypeReadWrite<P1> prw1,
                    List<Pair<P2A1, P2A2>> p2,
                    TypeReadWrite<P2A1> prw2a1,
                    TypeReadWrite<P2A2> prw2a2,
                    DataSourceW ds
                )  {
                    var buffer = new StringBuilder();
                   \s
                    buffer.append(""\"
                        select name from users where id = ""\");
                    buffer.append(" ? ");
                    buffer.append(""\"
                        and (name, age) in (""\");
                    for (var i = 0; i < p2.size(); i++) {
                        buffer.append(" ?, ? ");
                        if (i != p2.size() - 1)
                            buffer.append(", ");
                    }
                    buffer.append(""\"
                        )""\");
                   \s
                    var query = buffer.toString();
                   \s
                    try (var connection = source.w().getConnection()) {
                        try (var stmt = connection.prepareStatement(query)) {
                       \s
                            var n = 0;
                            prw1.set(stmt, ++n, p1);
                           \s
                            for (var element : p2) {
                                prw2a1.set(stmt, ++n, element.a1());
                                prw2a2.set(stmt, ++n, element.a2());
                            }
                           \s
                            stmt.execute();
                           \s
                            var rs = stmt.getResultSet()
                           \s
                           \s
                            var mapped = Stream.iterate(
                                rs, t -> {
                                    try {
                                        return t.next();
                                    } catch (SQLException e) {
                                        throw source.mapException(e);
                                    }
                                }, t -> t
                            ).map(t -> {
                                try {
                                    return
                                        Objects.requireNonNull(new StringReadWrite().get(rs, 1));
                                } catch (SQLException e) {
                                    throw source.mapException(e);
                                }
                            })
                            ;
                            return new UpdateResult<>(stmt.getLargeUpdateCount(), mapped.toList());
                       \s
                        }
                    } catch (SQLException e) {
                        throw source.mapException(e);
                    }
                }
                """,
            generate(
                t -> switch (t) {
                    case "java.lang.String" -> "StringReadWrite";
                    default -> throw new RuntimeException("not implemented");
                },
                12,
                SourceMode.DATASOURCE,
                new SingleQuery(List.of(
                    new TextPart("select name from users where id = "),
                    new SimpleParameter(null),
                    new TextPart("and (name, age) in ("),
                    new UnfoldParameter(2, null),
                    new TextPart(")")
                )),
                new AsDefault(),
                new FetchList(new ScalarType(NullMode.DEFAULT_NOT_NULL, "java.lang.String")),
                false,
                true
            )
        );
    }
}
