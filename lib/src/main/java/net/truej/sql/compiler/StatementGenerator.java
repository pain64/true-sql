package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.truej.sql.compiler.GLangParser.*;

public class StatementGenerator {

    public enum SourceMode {DATASOURCE, CONNECTION}
    public sealed interface QueryMode {
        List<InvocationsFinder.QueryPart> parts();
    }
    public record BatchedQuery(
        JCTree.JCExpression listDataExpression,
        JCTree.JCLambda extractor,
        List<InvocationsFinder.QueryPart> parts
    ) implements QueryMode { }

    public record SingleQuery(List<InvocationsFinder.QueryPart> parts) implements QueryMode { }

    public sealed interface StatementMode { }
    public record AsDefault() implements StatementMode { }
    public record AsCall(int[] outParametersIndexes) implements StatementMode { }
    public record AsGeneratedKeysIndices(int... columnIndexes) implements StatementMode { }
    public record AsGeneratedKeysColumnNames(String... columnNames) implements StatementMode { }

    public sealed interface FetchMode { }
    public sealed interface FetchTo extends FetchMode {
        FieldType toType();
    }

    // FIXME: .g cannot have has none???
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

    public static int unfoldArgumentsCount(@Nullable JCTree.JCLambda extractor) {
        return extractor == null ? 1 :
            ((JCTree.JCNewArray) extractor.body).elems.size();
    }

    // TODO: deal with nullability in DTO Fields
    // TODO: deal with nullability in return type
    public static String generate(
        Function<String, String> typeToRwClass,
        int lineNumber,
        SourceMode source,
        QueryMode query,
        StatementMode prepareAs,
        FetchMode fetchAs,
        boolean generateDto,
        boolean withUpdateCount
    ) {
        var out = new Out(new StringBuilder());

        if (
            generateDto &&
            fetchAs instanceof FetchTo to
            && to.toType() instanceof AggregatedType agg
        ) {
            DtoGenerator.generate(out, agg);
            var _ = out."\n";
        }

        var updateCountType = query instanceof BatchedQuery ? "long[]" : "Long";

        var wrapTypeWithUpdateCount = (BiFunction<@Nullable String, String, String>) (collectionT, t) -> {
            var boxed = MapperGenerator.boxedClassName(t);
            if (fetchAs instanceof FetchNone) return updateCountType;

            var toCollectionType = (Supplier<String>) () ->
                collectionT != null ? collectionT + "<" + boxed + ">" : boxed;

            if (!withUpdateCount) return toCollectionType.get();

            return fetchAs instanceof FetchStream
                ? STR."UpdateResultStream<\{updateCountType}, \{boxed}>"
                : STR."UpdateResult<\{updateCountType}, \{toCollectionType.get()}>";
        };

        // FIXME: remove duplication with AnnotationProcessor
        var methodName = switch (fetchAs) {
            case FetchNone _ -> "fetchNone";
            case FetchList _ -> "fetchList";
            case FetchOne _ -> "fetchOne";
            case FetchOneOrZero _ -> "fetchOneOrZero";
            case FetchStream _ -> "fetchStream";
        };

        var resultType = switch (fetchAs) {
            case FetchNone _ -> wrapTypeWithUpdateCount.apply(null, "Void");
            case FetchList f -> wrapTypeWithUpdateCount.apply("List", f.toType.javaClassName());
            case FetchOne f -> wrapTypeWithUpdateCount.apply(null, f.toType.javaClassName());
            case FetchOneOrZero f ->
                wrapTypeWithUpdateCount.apply(null, f.toType.javaClassName()); // FIXME: nullable
            case FetchStream f -> wrapTypeWithUpdateCount.apply("Stream", f.toType.javaClassName());
        };

        final WriteNext typeParameters;
        final WriteNext parametersCode;
        final WriteNext throwsClause;
        final WriteNext queryText;
        final WriteNext setParameters;
        final WriteNext execute;
        final String updateCount;

        switch (query) {
            case BatchedQuery bp -> {
                var simpleParameters = bp.parts.stream()
                    .filter(p -> p instanceof InvocationsFinder.SimpleParameter).toList();

                var tpList = Out.each(
                    simpleParameters, ", ", (o, i, _) ->
                        o."P\{i + 1}, E\{i + 1} extends Exception"
                );

                typeParameters = o -> o."<B, \{tpList}>";

                throwsClause = o -> {
                    var body = Out.each(
                        simpleParameters, ", ", (oo, i, _) -> oo."E\{i + 1}"
                    );
                    return o."throws \{body}";
                };

                var eachParameterDecl = Out.each(simpleParameters, ", ", (o, i, _) ->
                    o."""
                        ParameterExtractor<B, P\{i + 1}> pe\{i + 1},
                        TypeReadWrite<P\{i + 1}> prw\{i + 1}"""
                );

                parametersCode = o -> o."""
                    List<B> batch,
                    \{eachParameterDecl},""";

                var queryAgg = Out.each(bp.parts, "", (o, _, p) ->
                    p instanceof InvocationsFinder.TextPart tp ? o."\{tp.text()}" : o."?"
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

                execute = o -> o."var updateCount = stmt.executeLargeBatch();";
                updateCount = "updateCount";
            }
            case SingleQuery sp -> {
                var nonTextParts = sp.parts.stream()
                    .filter(p -> !(p instanceof InvocationsFinder.TextPart)).toList();

                var eachTypeParameter = Out.each(
                    nonTextParts, ", ", (o, i, p) -> switch (p) {
                        case InvocationsFinder.TextPart _ ->
                            throw new IllegalStateException("unreachable");
                        case InvocationsFinder.SimpleParameter _,
                             InvocationsFinder.InoutParameter _,
                             InvocationsFinder.OutParameter _ -> o."P\{i + 1}";
                        case InvocationsFinder.UnfoldParameter up -> {
                            var n = unfoldArgumentsCount(up.extractor());

                            if (up.extractor() == null) yield o."P\{i + 1}";

                            var elements = Out.each(
                                IntStream.range(0, n).boxed().toList(),
                                ", ", (oo, j, _) -> oo."P\{i + 1}E\{j + 1}"
                            );
                            yield o."P\{i + 1}, \{elements}";
                        }
                    }
                );

                typeParameters = nonTextParts.isEmpty() ? o -> o."" : o -> o."<\{eachTypeParameter}>";

                var eachParameter = Out.each(
                    nonTextParts, ",\n", (o, i, p) -> switch (p) {
                        case InvocationsFinder.TextPart _ ->
                            throw new IllegalStateException("unreachable");
                        case InvocationsFinder.SimpleParameter _,
                             InvocationsFinder.InoutParameter _ -> o."""
                            P\{i + 1} p\{i + 1},
                            TypeReadWrite<P\{i + 1}> prw\{i + 1}""";
                        case InvocationsFinder.OutParameter _ -> o."""
                            TypeReadWrite<P\{i + 1}> prw\{i + 1}""";
                        case InvocationsFinder.UnfoldParameter up -> {
                            var n = unfoldArgumentsCount(up.extractor());
                            if (up.extractor() == null) yield o."""
                                List<P\{i + 1}> p\{i + 1},
                                TypeReadWrite<P\{i + 1}> prw\{i + 1}e1""";

                            var extractorsAndRw = Out.each(
                                IntStream.range(0, n).boxed().toList(),
                                ",\n", (oo, j, _) -> oo."""
                                    ParameterExtractor<P\{i + 1}, P\{i + 1}E\{j + 1}> p\{i + 1}e\{j + 1},
                                    TypeReadWrite<P\{i + 1}E\{j + 1}> prw\{i + 1}e\{j + 1}"""
                            );

                            yield o."""
                                List<P\{i + 1}> p\{i + 1},
                                \{extractorsAndRw}""";
                        }
                    }
                );

                parametersCode = nonTextParts.isEmpty() ? o -> o."" : o -> o."\{eachParameter},";

                throwsClause = o -> null;
                queryText = o -> {
                    var pIndex = new int[]{1};

                    var generator = Out.each(
                        sp.parts, "\n", (oo, _, p) -> switch (p) {
                            case InvocationsFinder.TextPart t -> oo."""
                                buffer.append(""\"
                                    \{t.text()}""\");""";
                            case InvocationsFinder.SimpleParameter _,
                                 InvocationsFinder.InoutParameter _,
                                 InvocationsFinder.OutParameter _ -> {
                                pIndex[0]++;
                                yield oo."buffer.append(\" ? \");";
                            }
                            case InvocationsFinder.UnfoldParameter up -> {
                                var n = unfoldArgumentsCount(up.extractor());
                                var unfolded = Out.each(
                                    IntStream.range(0, n).boxed().toList(),
                                    ", ", (ooo, _, _) -> ooo."?"
                                );

                                yield oo."""
                                    for (var i = 0; i < p\{pIndex[0]}.size(); i++) {
                                        buffer.append(" (\{unfolded}) ");
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
                            case InvocationsFinder.TextPart _ ->
                                throw new IllegalStateException("unreachable");
                            case InvocationsFinder.SimpleParameter _,
                                 InvocationsFinder.InoutParameter _ ->
                                oo."prw\{i + 1}.set(stmt, ++n, p\{i + 1});";
                            case InvocationsFinder.OutParameter _ ->
                                oo."prw\{i + 1}.registerOutParameter(stmt, ++n);";
                            case InvocationsFinder.UnfoldParameter up -> {
                                var n = unfoldArgumentsCount(up.extractor());
                                var unfolded = Out.each(
                                    IntStream.range(0, n).boxed().toList(),
                                    "\n", (ooo, j, _) -> ooo."""
                                        prw\{i + 1}e\{j + 1}.set(stmt, ++n, \{up.extractor() == null ? "element" : STR."p\{i + 1}e\{j + 1}.get(element)"});"""
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
                updateCount = "stmt.getLargeUpdateCount()";
            }
        }

        var from = switch (source) {
            case DATASOURCE -> "DataSourceW source";
            case CONNECTION -> "ConnectionW source";
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

        // Generate DTO (recursive version)
        // Fetch single, Fetch from out parameters
        var getResultSet = (WriteNext) o -> switch (prepareAs) {
            case AsCall _ -> null;
            case AsDefault _ -> o."var rs = stmt.getResultSet();";
            case AsGeneratedKeysColumnNames _,
                 AsGeneratedKeysIndices _ -> o."var rs = stmt.getGeneratedKeys();";
        };

        var wrapResultWithUpdateCount = (Function<String, String>) result -> {
            var wrapper = fetchAs instanceof FetchStream ? "UpdateResultStream" : "UpdateResult";
            return withUpdateCount ? STR."new \{wrapper}<>(\{updateCount}, \{result})" : result;
        };

        var doFetch = (WriteNext) o -> switch (fetchAs) {
            case FetchNone _ -> withUpdateCount
                ? o."return \{updateCount};"
                : o."return null;";
            case FetchTo to -> {
                MapperGenerator.generate(
                    o, to.toType(), prepareAs instanceof AsCall ac ? ac.outParametersIndexes : null, typeToRwClass
                );

                yield switch (to) {
                    case FetchList _ -> o."""
                        return \{wrapResultWithUpdateCount.apply("mapped.toList()")};""";
                    case FetchOne _ -> prepareAs instanceof AsCall ?
                        o."""
                            return \{wrapResultWithUpdateCount.apply("mapped")};
                            """
                        : o."""
                        var iterator = mapped.iterator();
                        if (iterator.hasNext()) {
                            var result = iterator.next();
                            if (iterator.hasNext())
                                throw new TooMuchRowsException();

                            return \{wrapResultWithUpdateCount.apply("result")};
                        } else
                            throw new TooFewRowsException();
                        """;
                    case FetchOneOrZero _ -> o."""
                        var iterator = mapped.iterator();
                        if (iterator.hasNext()) {
                            var result = iterator.next();
                            if (iterator.hasNext())
                                throw new TooMuchRowsException();
                            return \{wrapResultWithUpdateCount.apply("result")};
                        }

                        return \{wrapResultWithUpdateCount.apply("null")};""";
                    case FetchStream _ -> o."""
                        return \{wrapResultWithUpdateCount.apply("mapped")};""";
                };
            }
        };

        var doWithPrepared = (WriteNext) o -> o."""
             \{setParameters}

             \{execute}

             \{getResultSet}

             \{doFetch}""";

        var acquireStatement = (WriteNext) o ->
            fetchAs instanceof FetchStream
                ? o."""
                var stmt = connection.\{prepare};
                try {

                   \{doWithPrepared}

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

                    \{doWithPrepared}

                }""";

        var doWithSource = (WriteNext) o -> switch (source) {
            case DATASOURCE -> o."""
                try (var connection = source.w.getConnection()) {
                    \{acquireStatement}
                } catch (SQLException e) {
                    throw source.mapException(e);
                }""";
            case CONNECTION -> o."""
                try {
                    var connection = source.w;
                    \{acquireStatement}
                } catch (SQLException e) {
                    throw source.mapException(e);
                }""";
        };

        var _ = out."""
            public static \{typeParameters}
            \{resultType} \{methodName}__line\{lineNumber}__(
                \{parametersCode}
                \{from}
            ) \{throwsClause} {
                \{queryText}
                \{doWithSource}
            }
            """;

        return out.buffer.toString();
    }
}
