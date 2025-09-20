package net.truej.sql.compiler;

import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.GLangParser.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.compiler.TrueSqlPlugin.boxedClassName;

public class MapperGenerator {

    private record GroupInfo(
        String keyClassName, int offset, int count, boolean isIndexNeeded
    ) { }

    private static GroupInfo dtoForGroupKeys(
        Out out, int offset, ListOfGroupField lgf
    ) {

        var locals = lgf.fields().stream()
            .filter(f -> f instanceof ScalarField)
            .map(f -> (ScalarField) f).toList();

        var keyField = locals.getFirst();

        var next = lgf.fields().stream()
            .filter(f -> f instanceof Aggregated)
            .map(f -> (Aggregated) f).toList();

        var indexedNext = new ArrayList<String>();

        var currentOffset = offset + locals.size();
        for (var i = 0; i < next.size(); i++) {

            switch (next.get(i)) {
                case ListOfGroupField lgf2 -> {
                    var groupInfo = dtoForGroupKeys(
                        out, currentOffset, lgf2
                    );

                    currentOffset += groupInfo.count;

                    indexedNext.add(
                        ", HashMap<" +
                        boxedClassName(groupInfo.keyClassName) + ", " + (
                            groupInfo.isIndexNeeded ? "I" + groupInfo.offset : lgf2.newJavaClassName())
                        + "> n" + i
                    );
                }
                case ListOfScalarField lsf2 -> {
                    currentOffset++;
                    indexedNext.add(
                        ", HashMap<" + boxedClassName(lsf2.binding().className()) + ", "
                        + boxedClassName(lsf2.binding().className()) + "> n" + i
                    );
                }
            }
        }

        if (!indexedNext.isEmpty())
            out.w(
                "record I", offset, "(", lgf.newJavaClassName(), " v",
                Out.each(indexedNext, "", (oo, __, t) -> oo.w(t)),
                ") {}\n"
            );

        return new GroupInfo(
            keyField.binding().className(), offset, currentOffset - offset, !indexedNext.isEmpty()
        );
    }

    private static int nextOperations(
        Function<String, String> typeToRwClass,
        Out out, int offset,
        String idx, String to, boolean toIsRecord, Aggregated agg
    ) {

        var getField = (BiFunction<Standard.Binding, Integer, String>) (b, i) ->
            "new " + typeToRwClass.apply(b.className()) + "().get(rs, " + (i + 1) + ")";

        var toCollection = (Function<String, String>) (collectionName) ->
            to + collectionName + (toIsRecord ? "()" : "");


        var withGroupHeader = (BiConsumer<Standard.Binding, WriteNext>) (binding, next) ->
            out.w(
                "var k", offset, " = ", getField.apply(binding, offset), ";\n",
                "if (k", offset, " != null) {\n",
                "    var e", offset, " = ", idx, ".get(k", offset, ");\n",
                "    if (e", offset, " == null) {\n",
                "    ", next, "\n",
                "}\n"
            );


        return switch (agg) {
            case ListOfScalarField lsf -> {
                withGroupHeader.accept(lsf.binding(), o -> o.w(
                    "    ", idx, ".put(k", offset, ", k", offset, ");\n",
                    "    ", toCollection.apply(lsf.name()), ".add(k", offset, ");\n}"
                ));

                yield 1;
            }
            case ListOfGroupField lgf -> {

                var locals = lgf.fields().stream()
                    .filter(f -> f instanceof ScalarField)
                    .map(f -> (ScalarField) f).toList();

                var keyField = locals.getFirst();

                var next = lgf.fields().stream()
                    .filter(f -> f instanceof Aggregated)
                    .map(f -> (Aggregated) f).toList();

                var isIndexed = !next.isEmpty();

                var mapFields = (WriteNext) o -> {
                    for (var i = 1; i < locals.size(); i++)
                        o.w(", ", wrapWithActualNullCheck(getField.apply(
                            locals.get(i).binding(), offset + i), locals.get(i).nullMode()), "\n");
                    return null;
                };

                withGroupHeader.accept(keyField.binding(), o -> {
                    o.w(
                        "    e", offset, " = ", (isIndexed ? "new I" + offset + "(" : ""), "\n",
                        "        new ", lgf.newJavaClassName(), "(\n",
                        "            k", offset, "\n",
                        "            ", mapFields, "\n",
                        "            ", Out.each(next, "\n", (oo, __, ___) -> oo.w(", new ArrayList<>()")), "\n",
                        "        )\n",
                        "    ", Out.each(next, "\n", (oo, __, ___) -> oo.w(", new HashMap<>()")), "\n",
                        "    ", (isIndexed ? ")" : ""), ";\n",
                        "    ", idx, ".put(k", offset, ", e", offset, ");\n",
                        "    ", toCollection.apply(lgf.name()), ".add(e", offset, (isIndexed ? ".v" : ""), ");\n}\n"
                    );

                    var currentOffset = offset + locals.size();
                    for (var i = 0; i < next.size(); i++)
                        currentOffset += nextOperations(
                            typeToRwClass, out, currentOffset,
                            "e" + offset + ".n" + i,
                            "e" + offset + ".v.",
                            lgf.isRecord(), next.get(i)
                        );

                    return null;
                });

                yield locals.size();
            }
        };
    }

    private static String wrapWithActualNullCheck(String expr, NullMode nullMode) {
        return nullMode != NullMode.EXACTLY_NULLABLE ?
            "EvenSoNullPointerException.check(" + expr + ")" : expr;
    }

    public static void generate(
        Out out, SourceMode source, FetchTo to, FetchToField toType,
        @Nullable int[] outParametersNumbers,
        Function<String, String> typeToRwClass
    ) {

        var from = outParametersNumbers != null ? "stmt" : "rs";

        var getField = (BiFunction<ScalarField, Integer, String>) (t, i) -> {
            var j = outParametersNumbers == null ? i : outParametersNumbers[i - 1];
            return "new " + typeToRwClass.apply(t.binding().className())
                   + "().get(" + from + "," + j + ")";
        };

        final WriteNext mapFields;
        final boolean hasAggregation;
        final String toDto;

        switch (toType) {
            case ListOfGroupField lgf -> {

                hasAggregation = lgf.fields().stream()
                    .anyMatch(f -> f instanceof Aggregated);

                if (hasAggregation) {

                    var groupInfo = dtoForGroupKeys(out, 0, lgf);

                    var mappingRoutine = (WriteNext) oo -> {
                        nextOperations(typeToRwClass, oo, 0, "i0", "_", false, lgf);
                        return null;
                    };

                    out.w(
                        "var i0 = new HashMap<",
                        boxedClassName(groupInfo.keyClassName), ", I0>();\n",
                        "var _result = new ArrayList<", lgf.newJavaClassName(), ">();\n\n",
                        "while (rs.next()) {\n",
                        "    ", mappingRoutine, "\n",
                        "}\n"
                    );

                    toDto = "";
                    mapFields = null;

                } else {
                    var flattened = lgf.fields().stream().map(f -> (ScalarField) f).toList();
                    var eachField = Out.each(flattened, ",\n", (o, i, field) -> o.w(
                        wrapWithActualNullCheck(getField.apply(field, i + 1), field.nullMode())
                    ));

                    toDto = lgf.newJavaClassName();
                    mapFields = o -> o.w(
                        "new ", toDto, " (\n",
                        "    ", eachField, "\n",
                        ")"
                    );
                }

            }
            case ScalarField sf -> {
                toDto = sf.binding().className();
                mapFields = o ->
                    o.w(wrapWithActualNullCheck(getField.apply(sf, 1), sf.nullMode()));
                hasAggregation = false;
            }
        }

        switch (to) {
            case FetchList __ -> {
                if (!hasAggregation)
                    out.w(
                        "var mapped = new ArrayList<", toDto, ">();\n",
                        "while (rs.next())\n",
                        "    mapped.add(\n",
                        "        ", mapFields, "\n",
                        "    );\n"
                    );
                else
                    out.w("var mapped = _result;\n");

            } case FetchOne __ -> {
                if (outParametersNumbers != null)
                    out.w("var mapped = ", mapFields, ";\n");
                else {
                    if (hasAggregation) out.w(
                        "if (_result.isEmpty())\n",
                        "    throw new TooFewRowsException();\n",
                        "var mapped = _result.getFirst();\n",
                        "if (_result.size() > 1)\n",
                        "    throw new TooMuchRowsException();\n"
                    );
                    else out.w(
                        "if (!rs.next())",
                        "    throw new TooFewRowsException();\n",
                        "var mapped = ", mapFields, ";\n",
                        "if (rs.next())\n",
                        "    throw new TooMuchRowsException();\n"
                    );
                }
            } case FetchOneOrZero __ -> {
                if (hasAggregation) out.w(
                    "var mapped = _result.isEmpty() ? null : _result.getFirst();\n",
                    "if (_result.size() > 1)\n",
                    "    throw new TooMuchRowsException();\n"
                );
                else out.w(
                    "var mapped = rs.next() ? ", mapFields, " : null;\n",
                    "if (rs.next())\n",
                    "    throw new TooMuchRowsException();\n"
                );
            } case FetchStream __ -> {
                if (hasAggregation)
                    out.w("var mapped = _result.stream();\n");
                else
                    out.w(
                        "var mapped = Stream.iterate(\n",
                        "    rs, t -> {\n",
                        "        try {\n",
                        "            return t.next();\n",
                        "        } catch (SQLException e) {\n",
                        "            throw source.mapException(e);\n",
                        "        }\n",
                        "    }, t -> t\n",
                        ").map(t -> {\n",
                        "    try {\n",
                        "        return\n",
                        "            ", mapFields, ";\n",
                        "    } catch (SQLException e) {\n",
                        "        throw source.mapException(e);\n",
                        "    }\n",
                        "});\n"
                    );

                var closeRoutine = (WriteNext) oo ->
                    source == SourceMode.DATASOURCE ? oo.w(
                        "try {\n" +
                        "    stmt.close();\n" +
                        "} finally {\n",
                        "    connection.close();\n",
                        "}"
                    ) : oo.w(
                        "stmt.close();"
                    );

                out.w(
                    "mapped = mapped.onClose(() -> {\n",
                    "    try {\n",
                    "    ", closeRoutine, "\n",
                    "     } catch(SQLException e) {\n" +
                    "         throw source.mapException(e);\n",
                    "     }\n",
                    "});\n"
                );
            }
        }
    }
}