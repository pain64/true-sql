package net.truej.sql.compiler;

import net.truej.sql.compiler.GLangParser.*;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.truej.sql.compiler.StatementGenerator.*;

public class MapperGenerator {

    private static List<ScalarField> flattenStartRow(List<Field> fields) {
        return fields.stream().flatMap(f ->
            switch (f) {
                case ListOfGroupField gf -> flattenStartRow(gf.fields()).stream();
                case ScalarField sf -> Stream.of(sf);
                case ListOfScalarField lsf -> Stream.of(
                    new ScalarField(
                        lsf.name(), lsf.nullMode(), lsf.binding()
                    )
                );
            }
        ).toList();
    }

    private static Void dtoForGroupKeys(
        Out out, int level, int offset, List<Field> fields
    ) {
        var next = fields.stream()
            .filter(f -> f instanceof Aggregated)
            .map(f -> (Aggregated) f).toList();

        if (!next.isEmpty()) {
            var locals = fields.stream()
                .filter(f -> f instanceof ScalarField)
                .map(f -> (ScalarField) f).toList();

            var groupKeys = Out.each(locals, ",\n", (o, i, field) ->
                o.w(boxedClassName(field.binding().className()), " c", offset + i + 1)
            );

            out.w(
                "record G", level, "(\n",
                "    ", groupKeys, "\n",
                ") {}\n"
            );

            // FIXME: use each ???
            var baseOffset = offset + locals.size();
            for (var n : next) {
                var nextFields = switch (n) {
                    case ListOfGroupField gf -> gf.fields();
                    case ListOfScalarField lsf -> List.of(
                        (Field) new ScalarField(
                            lsf.name(), lsf.nullMode(), lsf.binding()
                        )
                    );
                };

                dtoForGroupKeys(
                    out, level + 1, baseOffset, nextFields
                );

                baseOffset += nextFields.size();
            }
        }

        return null;
    }

    private static Void nextOperations(
        Out out, int level, int offset,
        String dtoJavaClassName, List<Field> fields
    ) {

        var locals = fields.stream()
            .filter(f -> f instanceof ScalarField)
            .map(f -> (ScalarField) f).toList();

        var next = fields.stream()
            .filter(f -> f instanceof Aggregated)
            .map(f -> (Aggregated) f).toList();

        var getField = (BiFunction<String, ScalarField, String>) (expr, st) ->
            wrapWithActualNullCheck(expr, st.nullMode());

        if (next.isEmpty()) {
            var dtoFilter = Out.each(locals, " ||\n", (o, i, __) ->
                o.w("java.util.Objects.nonNull(r.c", offset + i + 1, ")")
            );

            var dtoFieldsMapper = Out.each(locals, ",\n", (o, i, f) -> {
                var expr = "r.c" + (offset + i + 1);
                return o.w(getField.apply(expr, f));
            });

            var dtoMapper = dtoJavaClassName.startsWith("List<") ?
                dtoFieldsMapper :
                (WriteNext) o -> o.w(
                    "new ", dtoJavaClassName, "(\n",
                    "    ", dtoFieldsMapper, "\n",
                    ")"
                );

            out.w(
                ".filter(r ->\n",
                "    ", dtoFilter, "\n",
                ").map(r ->\n",
                "    ", dtoMapper, "\n",
                ").distinct()"
            );
        } else {
            var keysFilter = Out.each(locals, " ||\n", (o, i, __) ->
                o.w("java.util.Objects.nonNull(g", level, ".getKey().c", offset + i + 1, ")")
            );

            var keysMapper = Out.each(locals, ",\n", (o, i, __) ->
                o.w("r.c", offset + i + 1)
            );

            var dtoMapper = Out.each(locals, ",\n", (o, i, f) -> {
                var expr = "g" + level + ".getKey().c" + (offset + i + 1);
                return o.w(getField.apply(expr, f));
            });

            var nextTransform = (WriteNext) o -> {
                var baseOffset = offset + locals.size();

//                var line = each(next, ",", (o2, i, n) ->
//                    o."g\{level}.getValue().stream()\{1}.toList()"
//                );

                for (var i = 0; i < next.size(); i++) {
                    var n = next.get(i);

                    // FIXME: deduplicate with line 52
                    var nextFields = switch (n) {
                        case ListOfGroupField gf -> gf.fields();
                        case ListOfScalarField lsf -> List.of(
                            (Field) new ScalarField(
                                lsf.name(), lsf.nullMode(), lsf.binding()
                            )
                        );
                    };

                    o.w("g", level, ".getValue().stream()");
                    nextOperations(
                        out, level + 1, baseOffset,
                        switch (n) {
                            case ListOfGroupField gf -> gf.newJavaClassName();
                            case ListOfScalarField lsf -> "List<" + lsf.binding().className() + ">";
                        },
                        nextFields
                    );

                    o.w(".toList()");

                    if (i != next.size() - 1)
                        o.w(",\n");

                    baseOffset += nextFields.size();
                }

                return null;
            };

            out.w(
                ".collect(\n",
                "    java.util.stream.Collectors.groupingBy(\n",
                "        r -> new G", level, "(\n",
                "            ", keysMapper, "\n",
                "        ), java.util.LinkedHashMap::new, Collectors.toList()\n",
                "    )\n",
                ").entrySet().stream()\n",
                ".filter(g", level, " ->\n",
                "    ", keysFilter, "\n",
                ").map(g", level, " ->\n",
                "    new ", dtoJavaClassName, "(\n",
                "        ", dtoMapper, ",\n",
                "        ", nextTransform, "\n",
                "    )\n",
                ")"
            );
        }

        return null;
    }

    private static String wrapWithActualNullCheck(String expr, NullMode nullMode) {
        return nullMode != NullMode.EXACTLY_NULLABLE ?
            "EvenSoNullPointerException.check(" + expr + ")" : expr;
    }

    static String boxedClassName(String className) {
        return switch (className) {
            case "boolean" -> Boolean.class.getName();
            case "byte" -> Byte.class.getName();
            case "char" -> Character.class.getName();
            case "short" -> Short.class.getName();
            case "int" -> Integer.class.getName();
            case "long" -> Long.class.getName();
            case "float" -> Float.class.getName();
            case "double" -> Double.class.getName();
            default -> className;
        };
    }

    public static void generate(
        Out out, FetchToField toType,
        int[] outParametersNumbers,
        Function<String, String> typeToRwClass
    ) {

        var from = outParametersNumbers != null ? "stmt" : "rs"; // FIXME: rs vs t

        var getField = (BiFunction<ScalarField, Integer, String>) (t, i) -> {
            var j = outParametersNumbers == null ? i : outParametersNumbers[i - 1];
            return "new " + typeToRwClass.apply(t.binding().className())
                   + "().get(" + from + "," + j + ")";
        };

        final WriteNext extraDto;
        final WriteNext groupTransform;
        final WriteNext mapFields;

        switch (toType) {
            case ListOfGroupField lgf -> {
                final String toDto;

                var flattened = flattenStartRow(lgf.fields());
                var hasInnerAggregating = lgf.fields().stream()
                    .anyMatch(f -> f instanceof Aggregated);

                var eachField = Out.each(flattened, ",\n", (o, i, field) -> {
                    var expr = getField.apply(field, i + 1);
                    return o.w(
                        hasInnerAggregating ? expr : wrapWithActualNullCheck(expr, field.nullMode())
                    );
                });

                if (hasInnerAggregating) {
                    var rowFields = Out.each(flattened, ",\n", (o, i, field) ->
                        o.w(boxedClassName(field.binding().className()), " c", i + 1)
                    );

                    var groupKeysDto = (WriteNext) o ->
                        dtoForGroupKeys(o, 1, 0, lgf.fields());

                    toDto = "Row";
                    extraDto = o -> o.w(
                        "record Row(\n",
                        "    ", rowFields, "\n",
                        ") {}\n",
                        groupKeysDto, "\n",
                        "\n"
                    );
                    groupTransform = o ->
                        nextOperations(o, 1, 0, lgf.newJavaClassName(), lgf.fields());

                } else {
                    toDto = lgf.newJavaClassName();
                    extraDto = __ -> null;
                    groupTransform = __ -> null;
                }

                mapFields = o -> o.w(
                    "new ", toDto, " (\n",
                    "    ", eachField, "\n",
                    ")"
                );
            }
            case ScalarField sf -> {
                mapFields = o ->
                    o.w(wrapWithActualNullCheck(getField.apply(sf, 1), sf.nullMode()));
                extraDto = __ -> null;
                groupTransform = __ -> null;
            }
        }

        if (outParametersNumbers != null)
            out.w("var mapped = ", mapFields, ";");
        else
            out.w(
                extraDto, "\n",
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
                "})\n",
                groupTransform, ";\n"
            );
    }
}