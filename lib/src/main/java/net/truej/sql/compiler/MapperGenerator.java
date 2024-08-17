package net.truej.sql.compiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.truej.sql.compiler.StatementGenerator.*;

public class MapperGenerator {

    private static List<GLangParser.ScalarType> flattenStartRow(List<GLangParser.Field> fields) {
        return fields.stream().flatMap(f ->
            switch (f.type()) {
                case GLangParser.AggregatedType at -> flattenStartRow(at.fields()).stream();
                case GLangParser.ScalarType st -> Stream.of(st);
            }
        ).toList();
    }

    private static Void dtoForGroupKeys(
        Out out, int level, int offset, List<GLangParser.Field> fields
    ) {
        var next = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.AggregatedType).toList();

        if (!next.isEmpty()) {
            var locals = fields.stream()
                .filter(f -> f.type() instanceof GLangParser.ScalarType).toList();

            var groupKeys = Out.each(locals, ",\n", (o, i, field) ->
                o."\{boxedClassName(field.type().javaClassName())} c\{offset + i + 1}"
            );
            var _ = out."""
                record G\{level}(
                    \{groupKeys}
                ) {}
                """;

            // FIXME: use each ???
            var baseOffset = offset + locals.size();
            for (GLangParser.Field n : next) {
                dtoForGroupKeys(
                    out, level + 1, baseOffset,
                    ((GLangParser.AggregatedType) n.type()).fields()
                );

                baseOffset += ((GLangParser.AggregatedType) n.type()).fields().size();
            }
        }

        return null;
    }

    private static Void nextOperations(
        Out out, int level, int offset,
        String dtoJavaClassName, List<GLangParser.Field> fields
    ) {

        var locals = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.ScalarType)
            .toList();

        var next = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.AggregatedType).toList();

        var getField = (BiFunction<String, GLangParser.ScalarType, String>) (expr, st) ->
            STR."\{wrapWithActualNullCheck(expr, st.nullMode())}";

        if (next.isEmpty()) {
            var dtoFilter = Out.each(locals, " ||\n", (o, i, _) ->
                o."java.util.Objects.nonNull(r.c\{offset + i + 1})"
            );

            var dtoFieldsMapper = Out.each(locals, ",\n", (o, i, f) -> {
                var expr = STR."r.c\{offset + i + 1}";
                return ((Out) o)."\{getField.apply(expr, ((GLangParser.ScalarType) f.type()))}";
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
                        ."\{getField.apply(expr, ((GLangParser.ScalarType) f.type()))}";
            });

            var nextTransform = (WriteNext) o -> {
                var baseOffset = offset + locals.size();

//                var line = each(next, ",", (o2, i, n) ->
//                    o."g\{level}.getValue().stream()\{1}.toList()"
//                );

                for (var i = 0; i < next.size(); i++) {
                    var n = next.get(i);

                    var _ = o."g\{level}.getValue().stream()";
                    nextOperations(
                        out, level + 1, baseOffset,
                        ((GLangParser.AggregatedType) n.type()).javaClassName(),
                        ((GLangParser.AggregatedType) n.type()).fields()
                    );
                    var _ = o.".toList()";

                    if (i != next.size() - 1) {
                        var _ = o.",\n";
                    }

                    baseOffset += ((GLangParser.AggregatedType) n.type()).fields().size();
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

    private static String wrapWithActualNullCheck(String expr, GLangParser.NullMode nullMode) {
        return nullMode != GLangParser.NullMode.EXACTLY_NULLABLE ?
            STR."EvenSoNullPointerException.check(\{expr})" : expr;
    }

    static String boxedClassName(String className){
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
        Out out, GLangParser.FieldType toType,
        int[] outParametersIndexes,
        Function<String, String> typeToRwClass
    ) {

        var from = outParametersIndexes != null ? "stmt" : "rs"; // FIXME: rs vs t

        var getField = (BiFunction<GLangParser.ScalarType, Integer, String>) (t, i) -> {
            var j = outParametersIndexes == null ? i : outParametersIndexes[i - 1];
            return STR."new \{typeToRwClass.apply(t.javaClassName())}().get(\{from}, \{j}";
        };

        final WriteNext extraDto;
        final WriteNext groupTransform;
        final WriteNext mapFields;

        switch (toType) {
            case GLangParser.AggregatedType at -> {
                final String toDto;

                var flattened = flattenStartRow(at.fields());
                var hasGrouping = at.fields().stream()
                    .anyMatch(f -> f.type() instanceof GLangParser.AggregatedType);

                var eachField = Out.each(flattened, ",\n", (o, i, field) -> {
                    var expr = getField.apply(field, i + 1);
                    return ((Out) o)."\{hasGrouping ? expr : wrapWithActualNullCheck(expr, field.nullMode())})";
                });

                if (hasGrouping) {
                    var rowFields = Out.each(flattened, ",\n", (o, i, field) -> {
                        return ((Out) o)."\{boxedClassName(field.javaClassName())} c\{i + 1}";
                    });

                    var groupKeysDto = (WriteNext) o ->
                        dtoForGroupKeys(o, 1, 0, at.fields());

                    toDto = "Row";
                    extraDto = o -> o."""
                        record Row(
                            \{rowFields}
                        ) {}
                        \{groupKeysDto}
                        """;
                    groupTransform = o ->
                        nextOperations(o, 1, 0, at.javaClassName(), at.fields());

                } else {
                    toDto = at.javaClassName();
                    extraDto = _ -> null;
                    groupTransform = _ -> null;
                }

                mapFields = o -> o."""
                    new \{toDto} (
                        \{eachField}
                    )""";
            }
            case GLangParser.ScalarType st -> {
                mapFields = o ->
                    o."\{wrapWithActualNullCheck(getField.apply(st, 1), st.nullMode())})";
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