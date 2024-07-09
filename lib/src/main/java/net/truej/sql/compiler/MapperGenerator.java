package net.truej.sql.compiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

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
        StatementGenerator.Out out, int level, int offset, List<GLangParser.Field> fields
    ) {
        var next = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.AggregatedType).toList();

        if (!next.isEmpty()) {
            var locals = fields.stream()
                .filter(f -> f.type() instanceof GLangParser.ScalarType).toList();

            var groupKeys = StatementGenerator.Out.each(locals, ",\n", (o, i, field) ->
                o."\{field.type().javaClassName()} c\{offset + i + 1}"
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
        StatementGenerator.Out out, int level, int offset,
        String dtoJavaClassName, List<GLangParser.Field> fields
    ) {

        var locals = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.ScalarType).toList();

        var next = fields.stream()
            .filter(f -> f.type() instanceof GLangParser.AggregatedType).toList();

        if (next.isEmpty()) {
            var dtoFilter = StatementGenerator.Out.each(locals, " &&\n", (o, i, _) ->
                o."java.util.Objects.nonNull(r.c\{offset + i + 1})"
            );

            var dtoFieldsMapper = StatementGenerator.Out.each(locals, ",\n", (o, i, _) ->
                o."r.c\{offset + i + 1}"
            );

            var dtoMapper = dtoJavaClassName.startsWith("List<")
                ? dtoFieldsMapper : (StatementGenerator.WriteNext) o -> o."""
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
            var keysFilter = StatementGenerator.Out.each(locals, " &&\n", (o, i, _) ->
                o."java.util.Objects.nonNull(g\{level}.getKey().c\{offset + i + 1})"
            );

            var keysMapper = StatementGenerator.Out.each(locals, ",\n", (o, i, _) ->
                o."r.c\{offset + i + 1}"
            );

            var dtoMapper = StatementGenerator.Out.each(locals, ",\n", (o, i, _) ->
                o."g\{level}.getKey().c\{offset + i + 1}"
            );

            var nextTransform = (StatementGenerator.WriteNext) o -> {
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

    public static void generate(
        StatementGenerator.Out out, GLangParser.FieldType toType,
        int[] outParametersIndexes,
        Function<String, String> typeToRwClass
    ) {

        var from = outParametersIndexes != null ? "stmt" : "rs"; // FIXME: rs vs t

        var getField = (BiFunction<GLangParser.ScalarType, Integer, String>) (t, i) -> {
            var j = outParametersIndexes == null ? i : outParametersIndexes[i - 1];
            var base = STR."new \{typeToRwClass.apply(t.javaClassName())}().get(\{from}, \{j}";

            if (t.nullMode() != GLangParser.NullMode.EXACTLY_NULLABLE)
                return STR."EvenSoNullPointerException.check(\{base})";
            else
                return base;
        };


        final StatementGenerator.WriteNext extraDto;
        final StatementGenerator.WriteNext groupTransform;
        final StatementGenerator.WriteNext mapFields;

        switch (toType) {
            case GLangParser.AggregatedType at -> {
                final String toDto;

                var flattened = flattenStartRow(at.fields());
                var hasGrouping = at.fields().stream()
                    .anyMatch(f -> f.type() instanceof GLangParser.AggregatedType);

                var eachField = StatementGenerator.Out.each(flattened, ",\n", (o, i, field) ->
                    o."\{getField.apply(field, i + 1)})"
                );

                if (hasGrouping) {
                    var rowFields = StatementGenerator.Out.each(flattened, ",\n", (o, i, field) ->
                        o."\{field.javaClassName()} c\{i + 1}"
                    );

                    var groupKeysDto = (StatementGenerator.WriteNext) o ->
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
                    o."\{getField.apply(st, 1)})";
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