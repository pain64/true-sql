package com.truej.sql.v3.compiler;

import com.truej.sql.v3.config.TypeReadWrite;
import com.truej.sql.v3.fetch.UpdateResult;
import com.truej.sql.v3.prepare.Parameters;
import com.truej.sql.v3.source.DataSourceW;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.TrueSqlAnnotationProcessor.*;

public class StatementGenerator {

    public enum SourceMode {DATASOURCE, CONNECTION}
    public sealed interface QueryMode { }
    public record BatchedQuery(List<BatchedQueryPart> parts) implements QueryMode { }
    public record SingleQuery(List<SingleQueryPart> parts) implements QueryMode { }

    public sealed interface StatementMode { }
    public record AsDefault() implements StatementMode { }
    public record AsCall() implements StatementMode { }
    // FIXME
    public record AsGeneratedKeysIndices(int... columnIndexes) implements StatementMode { }
    public record AsGeneratedKeysColumnNames(String... columnNames) implements StatementMode { }

    public sealed interface FetchMode { }
    public sealed interface FetchTo extends FetchMode {
        FieldType toType();
    }

    public record FetchNone() implements FetchMode { }
    public record FetchOne(FieldType toType) implements FetchTo { }
    public record FetchOneOrZero(FieldType toType) implements FetchTo { }
    public record FetchList(FieldType toType) implements FetchTo { }
    public record FetchStream(FieldType toType) implements FetchTo { }

    interface WriteNext {
        Void write(Out out);
    }

    public static class Out implements StringTemplate.Processor<Void, RuntimeException> {
        public final StringBuilder buffer;
        int offset = 0;
        int currentColumn = 0;

        public Out(StringBuilder buffer) {
            this.buffer = buffer;
        }

        private void writeChar(Character ch) {
            buffer.append(ch);
            if (ch == '\n') {
                buffer.repeat(' ', offset);
                currentColumn = offset;
            } else
                currentColumn++;
        }

        private void writePart(String part) {
            for (var j = 0; j < part.length(); j++)
                writeChar(part.charAt(j));
        }

        @Override public Void process(StringTemplate template) throws RuntimeException {

            for (var i = 0; i < template.fragments().size(); i++) {
                writePart(template.fragments().get(i));

                if (i != template.fragments().size() - 1) {
                    var value = template.values().get(i);

                    if (value instanceof WriteNext next) {
                        var oldOffset = offset;
                        offset = currentColumn;
                        next.write(this);
                        offset = oldOffset;
                    } else
                        writePart(value.toString());
                }
            }

            return null;
        }

        interface EachNext<T> {
            Void write(Out out, Integer index, T element);
        }

        static <T> WriteNext each(List<T> list, String delimiter, EachNext<T> next) {
            return out -> {
                for (var i = 0; i < list.size(); i++) {
                    next.write(out, i, list.get(i));
                    if (i != list.size() - 1)
                        out.writePart(delimiter);
                }
                return null;
            };
        }
    }


    // Batched mode example
    // -- generated DTO's
    // <B, P1, E1 extends Exception, P2, E2 extends Exception>
    // Stream<@Nullable Long> fetchStream_1(
    //     List<B> batch,
    //     ParameterExtractor<B, P1, E1> pe1,
    //     ParameterExtractor<B, P2, E2> pe2,
    //     ParamSetter<P1> ps1,
    //     ParamSetter<P2> ps2,
    //     Source source
    // ) throws E1, E2 {
    //     source.withConnection( connection -> ...
    //         var queryText = "...";
    //         var stmt = connection.prepareStatement(...); // managed with try-with-resource or ...
    //
    //         -- set parameters
    //         for (var element : batch) {
    //             var p1 = pe1.get(element);
    //             var p2 = pe2.get(element);
    //             ps1.set(1, p1);
    //             ps2.set(2, p2);
    //         }
    //
    //         -- execute simple
    //         stmt.execute();
    //         -- execute batched
    //         var updateCount = stmt.executeBatch();
    //
    //         -- do mapping to dest type (ScalarType or AggregatedType)
    //         -- with nullability checks
    //         -- Stream<T> as output
    //
    //         -- transform by Fetcher: one, oneOrZero, list, stream
    //
    //        -- wrap with UpdateCount if needed
    // }
    // Single mode example (with unfold)
    // <P1, P2A1, P2A2, E extends Exception>
    // Long fetchOne_2(
    //     P1 p1,
    //     TypeReadWrite<P1> prw1,
    //     List<Tuple2<P2A1, P2A2>> u2,
    //     TypeReadWrite<P2A1> prw2a1,
    //     TypeReadWrite<P2A2> prw2a2,
    //     Source source
    // ) {
    //     ps1.set(++n, p1);
    //     for (var element : u2) {
    //         pb2a1.set(++n, element.a1);
    //         pba2.set(++n, element.a2);
    //     }
    // }


    public static String generate(
        int lineNumber,
        SourceMode source,
        QueryMode query,
        StatementMode prepareAs,
        FetchMode fetchAs,
        boolean withUpdateCount
    ) {
        var out = new Out(new StringBuilder());

        var updateCountType =
            query instanceof BatchedQuery ? "long[]" : "Long";
        var wrapWithUpdateCount = (Function<String, String>) t ->
            STR."UpdateResult<\{updateCountType}, \{t}>";

        // FIXME: remove duplication with AnnotationProcessor
        var methodName = switch (fetchAs) {
            case FetchNone _ -> "fetchNone";
            case FetchList _ -> "fetchList";
            case FetchOne _ -> "fetchOne";
            case FetchOneOrZero _ -> "fetchOneOrZero";
            case FetchStream _ -> "fetchStream";
        };

        var fetcherResultType = switch (fetchAs) {
            case FetchNone _ -> "Void";
            case FetchList f -> STR."List<\{f.toType.javaClassName()}>";
            case FetchOne f -> f.toType.javaClassName();
            case FetchOneOrZero f -> f.toType.javaClassName(); // FIXME: nullable
            case FetchStream f -> STR."Stream<\{f.toType.javaClassName()}>";
        };

        var resultType = withUpdateCount ? (
            fetchAs instanceof FetchNone
                ? updateCountType
                : wrapWithUpdateCount.apply(fetcherResultType)
        ) : fetcherResultType;

        final WriteNext typeParameters;
        final WriteNext parametersCode;
        final WriteNext throwsClause;
        final WriteNext queryText;
        final WriteNext setParameters;
        final WriteNext execute;

        switch (query) {
            case BatchedQuery bp -> {
                var simpleParameters = bp.parts.stream()
                    .filter(p -> p instanceof SimpleParameter).toList();

                var tpList = Out.each(
                    simpleParameters, ", ", (o, i, _) ->
                        o."P\{i + 1}, E\{i + 1} extends Exception"
                );

                typeParameters = o -> o."B, \{tpList}";

                throwsClause = o -> {
                    var body = Out.each(
                        simpleParameters, ", ", (oo, i, _) -> oo."E\{i + 1}"
                    );
                    return o."throws \{body}";
                };

                var eachParameterDecl = Out.each(simpleParameters, ", ", (o, i, _) ->
                    o."""
                        ParameterExtractor<B, P\{i + 1}, E\{i + 1}> pe\{i + 1},
                        TypeReadWrite<P\{i + 1}> prw\{i + 1}"""
                );

                parametersCode = o -> o."""
                    List<B> batch,
                    \{eachParameterDecl}""";

                var queryAgg = Out.each(bp.parts, "", (o, _, p) ->
                    p instanceof TextPart tp ? o."\{tp.text()}" : o."?"
                );

                // FIXME: check escaping
                queryText = o -> o."""
                    var query = ""\"
                        \{queryAgg}
                        ""\";
                    """;

                var eachSetParameter = Out.each(
                    simpleParameters, "\n", (o, i, p) -> o."""
                        var p\{i + 1} = pe\{i + 1}.get(element);
                        prw\{i + 1}.set(stmt, \{i + 1}, p\{i + 1});"""
                );
                setParameters = o -> o."""
                    for (var element : batch) {
                        \{eachSetParameter}
                        stmt.addBatch();
                    }""";

                execute = o -> o."var updateCount = stmt.executeBatch();";
            }
            case SingleQuery sp -> {
                var nonTextParts = sp.parts.stream()
                    .filter(p -> !(p instanceof TextPart)).toList();

                typeParameters = Out.each(
                    nonTextParts, ", ", (o, i, p) -> switch (p) {
                        case TextPart _ -> throw new IllegalStateException("unreachable");
                        case SimpleParameter _,
                            InoutParameter _,
                            OutParameter _ -> o."P\{i + 1}";
                        case UnfoldParameter up -> {
                            var attributes = Out.each(
                                IntStream.range(0, up.n()).boxed().toList(),
                                ", ", (oo, j, _) -> oo."P\{i + 1}A\{j + 1}"
                            );
                            yield o."\{attributes}";
                        }
                    }
                );

                parametersCode = Out.each(
                    nonTextParts, ",\n", (o, i, p) -> switch (p) {
                        case TextPart _ -> throw new IllegalStateException("unreachable");
                        case SimpleParameter _,
                            InoutParameter _,
                            OutParameter _ -> o."""
                                P\{i + 1} p\{i + 1},
                                TypeReadWrite<P\{i + 1}> prw\{i + 1}""";
                        case UnfoldParameter up -> {
                            var attributes = Out.each(
                                IntStream.range(0, up.n()).boxed().toList(),
                                ", ", (oo, j, _) -> oo."P\{i + 1}A\{j + 1}"
                            );
                            var container = (WriteNext) oo -> switch (up.n()) {
                                case 1 -> oo."\{attributes}";
                                case 2 -> oo."Pair<\{attributes}>";
                                case 3 -> oo."Triple<\{attributes}>";
                                case 4 -> oo."Quad<\{attributes}>";
                                default -> throw new IllegalStateException("unreachable");
                            };
                            var rw = Out.each(
                                IntStream.range(0, up.n()).boxed().toList(),
                                ",\n", (oo, j, _) ->
                                    oo."TypeReadWrite<P\{i + 1}A\{j + 1}> prw\{i + 1}a\{j + 1}"
                            );
                            yield o."""
                                List<\{container}> p\{i + 1},
                                \{rw}""";
                        }
                    }
                );

                throwsClause = o -> null;
                queryText = o -> {
                    var pIndex = new int[]{1};

                    var generator = Out.each(
                        sp.parts, "\n", (oo, _, p) -> switch (p) {
                            case TextPart t -> oo."""
                                buffer.append(""\"
                                    \{t.text()}""\");""";
                            case SimpleParameter _,
                                InoutParameter _,
                                OutParameter _ -> {
                                pIndex[0]++;
                                yield oo."buffer.append(\" ? \");";
                            }
                            case UnfoldParameter up -> {
                                var unfolded = Out.each(
                                    IntStream.range(0, up.n()).boxed().toList(),
                                    ", ", (ooo, _, _) -> ooo."?"
                                );
                                yield oo."""
                                    for (var i = 0; i < p\{pIndex[0]}.size(); i++) {
                                        buffer.append(" \{unfolded} ");
                                        if (i != p\{pIndex[0]++}.size() - 1)
                                            buffer.append(", ");
                                    }""";
                            }
                        }
                    );

                    return o."""
                        var buffer = new StringBuilder();

                        \{generator}

                        var query = buffer.toString();
                        """;
                };

                setParameters = o -> {
                    var setters = Out.each(
                        nonTextParts, "\n", (oo, i, p) -> switch (p) {
                            case TextPart _ -> throw new IllegalStateException("unreachable");
                            case SimpleParameter _,
                                InoutParameter _ -> oo."prw\{i + 1}.set(stmt, ++n, p\{i + 1});";
                            case OutParameter _ -> oo."";
                            case UnfoldParameter up -> {
                                var unfolded = Out.each(
                                    IntStream.range(0, up.n()).boxed().toList(),
                                    "\n", (ooo, j, _) -> ooo."""
                                        prw\{i + 1}a\{j + 1}.set(stmt, ++n, element.a\{j + 1}());"""
                                );
                                yield oo."""

                                    for (var element : p\{i + 1}) {
                                        \{unfolded}
                                    }""";
                            }
                        }
                    );

                    return o."""
                        var n = 0;
                        \{setters}""";
                };

                execute = o -> o."stmt.execute();";
            }
        }

        var from = switch (source) {
            case DATASOURCE -> "DataSourceW ds";
            case CONNECTION -> "ConnectionW cn";
        };

        var prepare = (WriteNext) o -> switch (prepareAs) {
            case AsCall _ -> o."prepareCall(query)";
            case AsDefault _ -> o."prepareStatement(query)";
            case AsGeneratedKeysColumnNames gk -> {
                var names = Arrays.stream(gk.columnNames).map(
                    cn -> STR."\"\{cn}\""
                ).collect(Collectors.joining(", "));

                yield o."prepareStatement(query, new String[] { \{names} })";

            }
            case AsGeneratedKeysIndices gk -> {
                var indexes = Arrays.stream(gk.columnIndexes)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(", "));
                yield o."prepareStatement(query, new int[] { \{indexes} })";
            }
        };

        var getResultSet = (WriteNext) o -> switch (prepareAs) {
            case AsCall asCall -> null;
            case AsDefault _ -> o."var rs = stmt.getResultSet()";
            case AsGeneratedKeysColumnNames _,
                AsGeneratedKeysIndices _ -> o."var rs = stmt.getGeneratedKeys()";
        };

        // TODO: wrap with update result
        var doFetch = (WriteNext) o -> switch (fetchAs) {
            case FetchNone _ -> null;
            case FetchTo to -> {
                MapperGenerator.generate(o, "XXX", List.of(
                    new Field(new ScalarType(false, "java.lang.String"), "x")
                ), (t, i) -> STR."fff_\{t}_\{i}");

                yield switch (to) {
                    case FetchList _ -> o."""
                        return mapped.toList()""";
                    case FetchOne _ -> o."""
                        var iterator = mapped.iterator()
                        if (iterator.hasNext()) {
                            var result = iterator.next();
                            if (iterator.hasNext())
                                throw new TooMuchRowsException();
                                                
                            return t.transform(base, executionResult, stmt, result);
                        } else
                            throw new TooFewRowsException();
                        """;
                    case FetchOneOrZero _ -> o."""
                        var iterator = mapped.iterator()
                        if (iterator.hasNext()) {
                            var result = iterator.next();
                            if (iterator.hasNext())
                                throw new TooMuchRowsException();
                            return result;
                        }
                                                
                        return null;""";
                    case FetchStream _ -> o."""
                        return mapped""";
                };
            }
        };

        var doWithPrepared = (WriteNext) o ->
            fetchAs instanceof FetchStream
                ? o."""
                var stmt = connection.\{prepare};
                try {

                    \{setParameters}

                    \{execute}

                    \{getResultSet}

                    \{doFetch}

                } catch (Exception e) {
                    try {
                        stmt.close();
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                    }
                    throw e;
                }"""
                : o."""
                try (var stmt = connection.\{prepare}) {

                    \{setParameters}

                    \{execute}

                    \{getResultSet}

                    \{doFetch}
                }""";

        var doWithSource = (WriteNext) o -> switch (source) {
            case DATASOURCE -> o."""
                try (var connection = source.w().getConnection()) {
                    \{doWithPrepared}
                } catch (SQLException e) {
                    throw source.mapException(e);
                }""";
            case CONNECTION -> o."""
                try {
                    var connection = source.w();
                    \{doWithPrepared}
                } catch (SQLException e) {
                    throw source.mapException(e);
                }""";
        };

        var _ = out."""
            <\{typeParameters}>
            \{resultType} \{methodName}__line\{lineNumber}__(
                \{parametersCode},
                \{from}
            ) \{throwsClause} {
                \{queryText}
                \{doWithSource}
            }
            """;

        return out.buffer.toString();
    }
}
