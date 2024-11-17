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
                o."\{boxedClassName(field.binding().className())} c\{offset + i + 1}"
            );
            var _ = out."""
                record G\{level}(
                    \{groupKeys}
                ) {}
                """;

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
            STR."\{wrapWithActualNullCheck(expr, st.nullMode())}";

        if (next.isEmpty()) {
            var dtoFilter = Out.each(locals, " ||\n", (o, i, _) ->
                o."java.util.Objects.nonNull(r.c\{offset + i + 1})"
            );

            var dtoFieldsMapper = Out.each(locals, ",\n", (o, i, f) -> {
                var expr = STR."r.c\{offset + i + 1}";
                return ((Out) o)."\{getField.apply(expr, f)}";
            });

            var dtoMapper = dtoJavaClassName.startsWith("List<")
                ? dtoFieldsMapper : (WriteNext) o -> o."""
                    new \{dtoJavaClassName}(
                        \{dtoFieldsMapper}
                    )""";

            var _ = out."""
                .filter(r ->
                    \{dtoFilter}
                ).map(r ->
                    \{dtoMapper}
                ).distinct()""";
        } else {
            var keysFilter = Out.each(locals, " ||\n", (o, i, _) ->
                o."java.util.Objects.nonNull(g\{level}.getKey().c\{offset + i + 1})"
            );

            var keysMapper = Out.each(locals, ",\n", (o, i, _) ->
                o."r.c\{offset + i + 1}"
            );

            var dtoMapper = Out.each(locals, ",\n", (o, i, f) -> {
                var expr = STR."g\{level}.getKey().c\{offset + i + 1}";
                return
                    ((Out) o)
                        ."\{getField.apply(expr, f)}";
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

                    var _ = o."g\{level}.getValue().stream()";
                    nextOperations(
                        out, level + 1, baseOffset,
                        switch (n) {
                            case ListOfGroupField gf -> gf.newJavaClassName();
                            case ListOfScalarField lsf -> "List<" + lsf.binding().className() + ">";
                        },
                        nextFields
                    );
                    var _ = o.".toList()";

                    if (i != next.size() - 1) {
                        var _ = o.",\n";
                    }

                    baseOffset += nextFields.size();
                }

                return null;
            };

            var _ = out."""
                .collect(
                    java.util.stream.Collectors.groupingBy(
                        r -> new G\{level}(
                            \{keysMapper}
                        ), java.util.LinkedHashMap::new, Collectors.toList()
                    )
                ).entrySet().stream()
                .filter(g\{level} ->
                    \{keysFilter}
                ).map(g\{level} ->
                    new \{dtoJavaClassName}(
                        \{dtoMapper},
                        \{nextTransform}
                    )
                )""";
        }

        return null;
    }

    private static String wrapWithActualNullCheck(String expr, NullMode nullMode) {
        return nullMode != NullMode.EXACTLY_NULLABLE ?
            STR."EvenSoNullPointerException.check(\{expr})" : expr;
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
        Out out, Field toType,
        int[] outParametersIndexes,
        Function<String, String> typeToRwClass
    ) {

        var from = outParametersIndexes != null ? "stmt" : "rs"; // FIXME: rs vs t

        var getField = (BiFunction<ScalarField, Integer, String>) (t, i) -> {
            var j = outParametersIndexes == null ? i : outParametersIndexes[i - 1];
            return STR."new \{typeToRwClass.apply(t.binding().className())}().get(\{from}, \{j}";
        };

        final WriteNext extraDto;
        final WriteNext groupTransform;
        final WriteNext mapFields;

        switch (toType) {
            case Aggregated gf -> {
                final String toDto;

                var javaClassName = switch (gf) {
                    case ListOfGroupField lgf -> lgf.newJavaClassName();
                    case ListOfScalarField lsf -> lsf.binding().className();
                };
                var fields = switch (gf) {
                    case ListOfGroupField lgf -> lgf.fields();
                    case ListOfScalarField lsf -> List.of((Field) lsf);
                };
                var flattened = flattenStartRow(fields);
                var hasInnerAggregating = fields.stream()
                    .anyMatch(f -> f instanceof Aggregated);

                var eachField = Out.each(flattened, ",\n", (o, i, field) -> {
                    var expr = getField.apply(field, i + 1);
                    return ((Out) o)."\{hasInnerAggregating ? expr : wrapWithActualNullCheck(expr, field.nullMode())})";
                });

                if (hasInnerAggregating) {
                    var rowFields = Out.each(flattened, ",\n", (o, i, field) ->
                        ((Out) o)."\{boxedClassName(field.binding().className())} c\{i + 1}"
                    );

                    var groupKeysDto = (WriteNext) o ->
                        dtoForGroupKeys(o, 1, 0, fields);

                    toDto = "Row";
                    extraDto = o -> o."""
                        record Row(
                            \{rowFields}
                        ) {}
                        \{groupKeysDto}
                        """;
                    groupTransform = o ->
                        nextOperations(o, 1, 0, javaClassName, fields);

                } else {
                    toDto = javaClassName;
                    extraDto = _ -> null;
                    groupTransform = _ -> null;
                }

                mapFields = o -> o."""
                    new \{toDto} (
                        \{eachField}
                    )""";
            }
            case ScalarField sf -> {
                mapFields = o ->
                    o."\{wrapWithActualNullCheck(getField.apply(sf, 1), sf.nullMode())})";
                extraDto = _ -> null;
                groupTransform = _ -> null;
            }
        }

        if (outParametersIndexes != null) {
            var _ = out."""
                var mapped = \{mapFields};
                """;
        } else {
            var _ = out."""
            \{extraDto}
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
                        \{mapFields};
                } catch (SQLException e) {
                    throw source.mapException(e);
                }
            })
            \{groupTransform};
            """;
        }
    }
}