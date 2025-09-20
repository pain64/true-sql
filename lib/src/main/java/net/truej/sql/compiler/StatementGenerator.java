package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import net.truej.sql.source.DataSourceW;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.TrueSqlPlugin.boxedClassName;
import static net.truej.sql.compiler.TrueSqlPlugin.classNameToSourceCodeType;

public class StatementGenerator {

    public enum SourceMode {DATASOURCE, CONNECTION}
    public sealed interface Query {
        List<InvocationsFinder.QueryPart> parts();
    }
    public record BatchedQuery(
        JCTree.JCExpression listDataExpression,
        JCTree.JCLambda extractor,
        List<InvocationsFinder.QueryPart> parts
    ) implements Query { }

    public record SingleQuery(List<InvocationsFinder.QueryPart> parts) implements Query { }

    public sealed interface StatementMode { }
    public sealed interface StatementLike extends StatementMode { }
    public record AsDefault() implements StatementLike { }
    public record AsCall() implements StatementMode { }
    public sealed interface AsGeneratedKeys extends StatementLike { }
    public record AsGeneratedKeysIndices(int... columnIndexes) implements AsGeneratedKeys { }
    public record AsGeneratedKeysColumnNames(String... columnNames) implements AsGeneratedKeys { }

    public sealed interface FetchMode { }
    public sealed interface FetchTo extends FetchMode {
        FetchToField toField();
    }

    // FIXME: .g cannot have has none???
    public record FetchNone() implements FetchMode { }
    public record FetchOne(FetchToField toField) implements FetchTo { }
    public record FetchOneOrZero(FetchToField toField) implements FetchTo { }
    public record FetchList(FetchToField toField) implements FetchTo { }
    public record FetchStream(FetchToField toField) implements FetchTo { }

    interface WriteNext {
        Void write(Out out);
    }

    public static class Out {
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

        public Void w(Object... fragments) throws RuntimeException {
            for (var value : fragments) {
                if (value instanceof WriteNext next) {
                    var oldOffset = offset;
                    offset = currentColumn;
                    next.write(this);
                    offset = oldOffset;
                } else
                    writePart(value.toString());
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

    // TODO: deal with nullability in return type
    public static String generate(
        Function<String, String> typeToRwClass,
        int lineNumber,
        SourceMode source,
        Query query,
        StatementMode prepareAs,
        FetchMode fetchAs,
        boolean generateDto,
        boolean withUpdateCount
    ) {
        var out = new Out(new StringBuilder());

        if (
            generateDto &&
            fetchAs instanceof FetchTo to
            && to.toField() instanceof ListOfGroupField agg
        ) {
            DtoGenerator.generate(out, agg);
            out.w("\n");
        }

        var updateCountType = query instanceof BatchedQuery ? "long[]" : "Long";

        var wrapTypeWithUpdateCount = (BiFunction<@Nullable String, String, String>) (collectionT, t) -> {
            var boxed = boxedClassName(t);
            if (fetchAs instanceof FetchNone) return updateCountType;

            var toCollectionType = (Supplier<String>) () ->
                collectionT != null ? collectionT + "<" + boxed + ">" : boxed;

            if (!withUpdateCount) return toCollectionType.get();

            return fetchAs instanceof FetchStream
                ? "UpdateResultStream<" + updateCountType + ", " + boxed + ">"
                : "UpdateResult<" + updateCountType + ", " + toCollectionType.get() + ">";
        };

        // FIXME: remove duplication with AnnotationProcessor
        var methodName = switch (fetchAs) {
            case FetchNone __ -> "fetchNone";
            case FetchList __ -> "fetchList";
            case FetchOne __ -> "fetchOne";
            case FetchOneOrZero __ -> "fetchOneOrZero";
            case FetchStream __ -> "fetchStream";
        };

        var resultType = switch (fetchAs) {
            case FetchNone __ -> wrapTypeWithUpdateCount.apply(null, "Void");
            case FetchTo to -> {
                var toSourceCodeType = switch (to.toField()) {
                    case ScalarField sf -> classNameToSourceCodeType(sf.binding().className());
                    case ListOfGroupField lgf -> lgf.newJavaClassName();
                };

                var wrapWith = switch (to) {
                    case FetchOne __ -> null;
                    case FetchOneOrZero __ -> null;
                    case FetchList __ -> "List";
                    case FetchStream __ -> "Stream";
                };
                yield wrapTypeWithUpdateCount.apply(wrapWith, toSourceCodeType);
            }
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
                    .filter(p -> p instanceof InvocationsFinder.InParameter).toList();

                var tpList = Out.each(
                    simpleParameters, ", ", (o, i, __) ->
                        o.w("P", i + 1, ", E", i + 1, " extends Exception")
                );

                typeParameters = o -> o.w("<B, ", tpList, ">");

                throwsClause = o -> {
                    var body = Out.each(
                        simpleParameters, ", ", (oo, i, __) -> oo.w("E", i + 1)
                    );
                    return o.w("throws ", body);
                };

                var eachParameterDecl = Out.each(simpleParameters, ", ", (o, i, __) ->
                    o.w(
                        "Function<B, P", i + 1, "> pe", i + 1, ",\n",
                        "TypeReadWrite<P", i + 1, "> prw", i + 1
                    )
                );

                parametersCode = o -> o.w(
                    "List<B> batch,\n",
                    eachParameterDecl, ","
                );

                var queryAgg = Out.each(bp.parts, "", (o, __, p) ->
                    p instanceof InvocationsFinder.TextPart tp ? o.w(tp.text()) : o.w("?")
                );

                // FIXME: check escaping
                queryText = o -> o.w("var query = \"\"\"\n", queryAgg, "\"\"\"");

                var eachSetParameter = Out.each(
                    simpleParameters, "\n", (o, i, __) -> o.w(
                        "var p", i + 1, " = pe", i + 1, ".apply(element);\n",
                        "prw", i + 1, ".set(stmt, ", i + 1, ", p", i + 1, ");\n"
                    )
                );
                setParameters = o -> o.w(
                    "for (var element : batch) {\n",
                    "    ", eachSetParameter, "\n",
                    "    stmt.addBatch();\n",
                    "}"
                );

                execute = o -> o.w("var updateCount = stmt.executeLargeBatch();");
                updateCount = "updateCount";
            }
            case SingleQuery sp -> {
                var parameterParts = sp.parts.stream()
                    .filter(p -> p instanceof InvocationsFinder.ParameterPart)
                    .map(p -> (InvocationsFinder.ParameterPart) p)
                    .toList();

                var eachTypeParameter = Out.each(
                    parameterParts, ", ", (o, i, p) -> switch (p) {
                        case InvocationsFinder.SingleParameter __ -> o.w("P", i + 1);
                        case InvocationsFinder.UnfoldParameter up -> {
                            var n = unfoldArgumentsCount(up.extractor());

                            if (up.extractor() == null) yield o.w("P", i + 1);

                            var elements = Out.each(
                                IntStream.range(0, n).boxed().toList(),
                                ", ", (oo, j, __) -> oo.w("P", i + 1, "E", j + 1)
                            );
                            yield o.w("P", i + 1, ", ", elements);
                        }
                    }
                );

                typeParameters = parameterParts.isEmpty()
                    ? o -> o.w("") : o -> o.w("<", eachTypeParameter, ">");

                var eachParameter = Out.each(
                    parameterParts, ",\n", (o, i, p) -> switch (p) {
                        case InvocationsFinder.InOrInoutParameter __ -> o.w(
                            "P", i + 1, " p", i + 1, " ,\n",
                            "TypeReadWrite<P", i + 1, "> prw", i + 1
                        );
                        case InvocationsFinder.OutParameter __ ->
                            o.w("TypeReadWrite<P", i + 1, "> prw", i + 1);
                        case InvocationsFinder.UnfoldParameter up -> {
                            var n = unfoldArgumentsCount(up.extractor());
                            if (up.extractor() == null) yield o.w(
                                "List<P", i + 1, "> p", i + 1, ",\n",
                                "TypeReadWrite<P", i + 1, "> prw", i + 1, "e1"
                            );

                            var extractorsAndRw = Out.each(
                                IntStream.range(0, n).boxed().toList(),
                                ",\n", (oo, j, __) -> oo.w(
                                    "Function<P", i + 1, ", P", i + 1, "E", j + 1, "> p", i + 1, "e", j + 1, ",\n",
                                    "TypeReadWrite<P", i + 1, "E", j + 1, "> prw", i + 1, "e", j + 1
                                )
                            );

                            yield o.w(
                                "List<P", i + 1, "> p", i + 1, ",\n",
                                extractorsAndRw
                            );
                        }
                    }
                );

                parametersCode = parameterParts.isEmpty()
                    ? o -> o.w("") : o -> o.w(eachParameter, ",");

                throwsClause = o -> null;
                queryText = o -> {
                    var pIndex = new int[]{1};

                    if (sp.parts.stream().anyMatch(
                        p -> p instanceof InvocationsFinder.UnfoldParameter
                    ))
                        return o.w(
                            "var buffer = new StringBuilder();\n\n",
                            Out.each(sp.parts, "\n", (oo, __, p) -> switch (p) {
                                case InvocationsFinder.TextPart t -> oo.w(
                                    "buffer.append(\"\"\"\n", t.text(), "\"\"\");"
                                );
                                case InvocationsFinder.SingleParameter ___ -> {
                                    pIndex[0]++;
                                    yield oo.w("buffer.append(\" ? \");");
                                }
                                case InvocationsFinder.UnfoldParameter up -> {
                                    var n = unfoldArgumentsCount(up.extractor());
                                    var unfolded = Out.each(
                                        IntStream.range(0, n).boxed().toList(),
                                        ", ", (ooo, ___, ____) -> ooo.w("?")
                                    );

                                    yield oo.w(
                                        "for (var i = 0; i < p", pIndex[0], ".size(); i++) {\n",
                                        "    buffer.append(\" (", unfolded, ") \");\n",
                                        "    if (i != p", pIndex[0]++, ".size() - 1)\n",
                                        "        buffer.append(\", \");\n",
                                        "}"
                                    );
                                }
                            }), "\n\n",
                            "var query = buffer.toString()"
                        );

                    else
                        return out.w("var query = \"\"\"\n", Out.each(sp.parts, "\n",
                            (oo, __, p) -> p instanceof InvocationsFinder.TextPart t ?
                                oo.w(t.text()) : oo.w(" ? ")
                        ), "\"\"\"");
                };

                setParameters = o -> {
                    var setters = Out.each(
                        parameterParts, "\n", (oo, i, p) -> switch (p) {
                            case InvocationsFinder.InParameter __ ->
                                oo.w("prw", i + 1, ".set(stmt, ++n, p", i + 1, ");");
                            case InvocationsFinder.InoutParameter __ -> oo.w(
                                "prw", i + 1, ".set(stmt, ++n, p", i + 1, ");\n",
                                "prw", i + 1, ".registerOutParameter(stmt, n);"
                            );
                            case InvocationsFinder.OutParameter __ ->
                                oo.w("prw", i + 1, ".registerOutParameter(stmt, ++n);");
                            case InvocationsFinder.UnfoldParameter up -> {
                                var n = unfoldArgumentsCount(up.extractor());
                                var unfolded = Out.each(
                                    IntStream.range(0, n).boxed().toList(),
                                    "\n", (ooo, j, __) -> ooo.w(
                                        "prw", i + 1, "e", j + 1, ".set(stmt, ++n, ",
                                        up.extractor() == null
                                            ? "element"
                                            : "p" + (i + 1) + "e" + (j + 1) + ".apply(element)",
                                        ");")
                                );
                                yield oo.w(
                                    "\n",
                                    "for (var element : p", i + 1, ") {\n",
                                    "    ", unfolded, "\n",
                                    "}"
                                );
                            }
                        }
                    );

                    return o.w(
                        "var n = 0;\n",
                        setters
                    );
                };

                execute = o -> o.w("stmt.execute();");
                updateCount = "stmt.getLargeUpdateCount()";
            }
        }

        var from = switch (source) {
            case DATASOURCE -> "DataSourceW source";
            case CONNECTION -> "ConnectionW source";
        };

        var prepare = (WriteNext) o -> switch (prepareAs) {
            case AsCall __ -> o.w("prepareCall(query)");
            case AsDefault __ -> o.w("prepareStatement(query)");
            case AsGeneratedKeysColumnNames gk -> {
                var names = Arrays.stream(gk.columnNames).map(
                    cn -> "\"" + cn + "\""
                ).collect(Collectors.joining(", "));

                yield o.w("prepareStatement(query, new String[] { ", names, " })");

            }
            case AsGeneratedKeysIndices gk -> {
                var indexes = Arrays.stream(gk.columnIndexes)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(", "));
                yield o.w("prepareStatement(query, new int[] { ", indexes, " })");
            }
        };

        // Generate DTO (recursive version)
        // Fetch single, Fetch from out parameters
        var getResultSet = (WriteNext) o -> switch (prepareAs) {
            case AsCall __ -> null;
            case AsDefault __ -> o.w("var rs = stmt.getResultSet();");
            case AsGeneratedKeys __ -> o.w("var rs = stmt.getGeneratedKeys();");
        };

        var doFetch = (WriteNext) o -> switch (fetchAs) {
            case FetchNone __ -> withUpdateCount
                ? o.w("return ", updateCount, ";")
                : o.w("return null;");
            case FetchTo to -> {

                MapperGenerator.generate(
                    o, source, to, to.toField(),
                    prepareAs instanceof AsCall ? InvocationsHandler.getOutParametersNumbers(query) : null,
                    typeToRwClass
                );

                var wrapper = fetchAs instanceof FetchStream ? "UpdateResultStream" : "UpdateResult";
                yield out.w(
                    "return ", withUpdateCount ?
                        "new " + wrapper + "<>(" + updateCount + ", mapped)" : "mapped", ";\n"
                );
            }
        };

        var doWithPrepared = (WriteNext) o -> o.w(
            setParameters, "\n",
            "\n",
            execute, "\n",
            "\n",
            getResultSet, "\n",
            "\n",
            doFetch
        );

        var acquireStatement = (WriteNext) o ->
            fetchAs instanceof FetchStream ?
                o.w(
                    "var stmt = connection.", prepare, ";\n",
                    "try {\n",
                    "\n",
                    "    ", doWithPrepared, "\n",
                    "\n",
                    "} catch (Exception e) {\n",
                    "    try {\n",
                    "        stmt.close();\n",
                    "    } catch (Exception e2) {\n",
                    "        e.addSuppressed(e2);\n",
                    "    }\n",
                    "    throw e;\n",
                    "}"
                ) :
                o.w(
                    "try (var stmt = connection.", prepare, ") {\n",
                    "\n",
                    "    ", doWithPrepared, "\n",
                    "}"
                );

        var handleSqlException = (WriteNext) o -> o.w(
            "} catch (SQLException e) {\n",
            "    throw source.mapException(e);\n",
            "}"
        );

        var doWithSource = (WriteNext) o -> switch (source) {
            case DATASOURCE -> fetchAs instanceof FetchStream ?
                o.w(
                    "try {\n",
                    "    var connection = source.w.getConnection();\n",
                    "    ", acquireStatement, "\n",
                    handleSqlException
                ) :
                o.w(
                    "try (var connection = source.w.getConnection()) {\n",
                    "    ", acquireStatement, "\n",
                    handleSqlException
                );
            case CONNECTION -> o.w(
                "try {\n",
                "    var connection = source.w;\n",
                "    ", acquireStatement, "\n",
                handleSqlException
            );
        };

        out.w(
            "public static ", typeParameters, "\n",
            resultType, " ", methodName, "__line", lineNumber, "__(\n",
            "    ", parametersCode, "\n",
            "    ", from, "\n",
            ") ", throwsClause, " {\n",
            "    ", queryText, ";\n",
            "    ", doWithSource, "\n",
            "}\n"
        );

        return out.buffer.toString();
    }
}
