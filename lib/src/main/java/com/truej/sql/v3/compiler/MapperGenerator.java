package com.truej.sql.v3.compiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.StatementGenerator.*;
import static com.truej.sql.v3.compiler.StatementGenerator.Out.each;

public class MapperGenerator {

    private static List<ScalarType> flattenStartRow(List<Field> fields) {
        return fields.stream().flatMap(f ->
            switch (f.type()) {
                case AggregatedType at -> flattenStartRow(at.fields()).stream();
                case ScalarType st -> Stream.of(st);
            }
        ).toList();
    }

    private static Void dtoForGroupKeys(
        Out out, int level, int offset, List<Field> fields
    ) {
        var next = fields.stream()
            .filter(f -> f.type() instanceof AggregatedType).toList();

        if (!next.isEmpty()) {
            var locals = fields.stream()
                .filter(f -> f.type() instanceof ScalarType).toList();

            var groupKeys = each(locals, ",\n", (o, i, field) ->
                o."\{field.type().javaClassName()} c\{offset + i + 1}"
            );
            var _ = out."""
                record G\{level}(
                    \{groupKeys}
                ) {}
                """;

            // FIXME: use each ???
            var baseOffset = offset + locals.size();
            for (Field n : next) {
                dtoForGroupKeys(
                    out, level + 1, baseOffset,
                    ((AggregatedType) n.type()).fields()
                );

                baseOffset += ((AggregatedType) n.type()).fields().size();
            }
        }

        return null;
    }

    private static Void nextOperations(
        Out out, int level, int offset,
        String dtoJavaClassName, List<Field> fields
    ) {

        var locals = fields.stream()
            .filter(f -> f.type() instanceof ScalarType).toList();

        var next = fields.stream()
            .filter(f -> f.type() instanceof AggregatedType).toList();

        if (next.isEmpty()) {
            var dtoFilter = each(locals, " &&\n", (o, i, _) ->
                o."java.util.Objects.nonNull(r.c\{offset + i + 1})"
            );

            var dtoFieldsMapper = each(locals, ",\n", (o, i, _) ->
                o."r.c\{offset + i + 1}"
            );

            var dtoMapper = dtoJavaClassName == null
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
            var keysFilter = each(locals, " &&\n", (o, i, _) ->
                o."java.util.Objects.nonNull(g\{level}.getKey().c\{offset + i + 1})"
            );

            var keysMapper = each(locals, ",\n", (o, i, _) ->
                o."r.c\{offset + i + 1}"
            );

            var dtoMapper = each(locals, ",\n", (o, i, _) ->
                o."g\{level}.getKey().c\{offset + i + 1}"
            );

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
                        ((AggregatedType) n.type()).javaClassName(),
                        ((AggregatedType) n.type()).fields()
                    );
                    var _ = o.".toList()";

                    if (i != next.size() - 1) {
                        var _ = o.",\n";
                    }

                    baseOffset += ((AggregatedType) n.type()).fields().size();
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
        Out out, FieldType toType, boolean isFromOutParameters,
        Function<String, String> typeToGetField
    ) {

        var from = isFromOutParameters ? "stmt" : "rs"; // FIXME: rs vs t

        var getField = (BiFunction<ScalarType, Integer, String>) (t, i) -> {
            var base = STR."new \{typeToGetField.apply(t.javaClassName())}().get(\{from}, \{i}";

            if (t.nullMode() != NullMode.EXACTLY_NULLABLE)
                return STR."Objects.requireNonNull(\{base})";
            else
                return base;
        };


        final WriteNext extraDto;
        final WriteNext groupTransform;
        final WriteNext mapFields;

        switch (toType) {
            case AggregatedType at -> {
                final String toDto;

                var flattened = flattenStartRow(at.fields());
                var hasGrouping = at.fields().stream()
                    .anyMatch(f -> f.type() instanceof AggregatedType);

                var eachField = each(flattened, ",\n", (o, i, field) ->
                    o."\{getField.apply(field, i + 1)})"
                );

                if (hasGrouping) {
                    var rowFields = each(flattened, ",\n", (o, i, field) ->
                        o."\{field.javaClassName()} c\{i + 1}"
                    );

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
            case ScalarType st -> {
                mapFields = o ->
                    o."\{getField.apply(st, 1)})";
                extraDto = _ -> null;
                groupTransform = _ -> null;
            }
        }

        if (isFromOutParameters) {
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