package com.truej.sql.v3.compiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.truej.sql.v3.compiler.GLangParser.*;

public class MapperGenerator {

    private static <T> String lc(List<T> list, int i) {
        return i != list.size() - 1 ? "," : "";
    }

    private static <T> String la(List<T> list, int i) {
        return i != list.size() - 1 ? "&&" : "";
    }

    private static void w(StringBuilder out, int deep, String... tokens) {
        out.repeat(" ", deep);
        for (var token : tokens) out.append(token);
    }

    private static void wl(StringBuilder out, int deep, String... tokens) {
        w(out, deep, tokens); out.append('\n');
    }

    private static List<ScalarType> flattenStartRow(List<Field> fields) {
        return fields.stream().flatMap(f ->
            switch (f.type()) {
                case AggregatedType at -> flattenStartRow(at.fields()).stream();
                case ScalarType st -> Stream.of(st);
            }
        ).toList();
    }

    private static void nextOperations(
        int level, int offset, int deep,
        StringBuilder extraDto, StringBuilder out,
        String dtoJavaClassName, List<Field> fields
    ) {

        var locals = fields.stream()
            .filter(f -> f.type() instanceof ScalarType).toList();

        var next = fields.stream()
            .filter(f -> f.type() instanceof AggregatedType).toList();

        if (next.isEmpty()) {
            wl(out, 0, ".filter(r ->");

            for (var i = 0; i < locals.size(); i++)
                wl(
                    out, deep + 4, "java.util.Objects.nonNull(r.c",
                    String.valueOf(offset + i + 1), ") ", la(locals, i)
                );

            wl(out, deep, ").map(r ->");
            if (dtoJavaClassName != null)
                wl(out, deep, "    new ", dtoJavaClassName, "(");

            for (var i = 0; i < locals.size(); i++)
                wl(out, deep + 8, "r.c", String.valueOf(offset + i + 1), lc(locals, i));

            if (dtoJavaClassName != null)
                wl(out, deep, "    )");

            w(out, deep, ")");
            w(out, 0, ".distinct()");
        } else {

            wl(extraDto, 4, "record G", String.valueOf(level), "(");
            for (var i = 0; i < locals.size(); i++)
                wl(
                    extraDto, 8,
                    locals.get(i).type().javaClassName(), " c", String.valueOf(offset + i + 1),
                    lc(locals, i)
                );
            wl(extraDto, 4, ") {}");

            wl(out, 0, ".collect(");
            wl(out, deep, "    java.util.stream.Collectors.groupingBy(");
            wl(out, deep, "        r -> new G", String.valueOf(level), "(");

            for (var i = 0; i < locals.size(); i++)
                wl(out, deep + 12, "r.c", String.valueOf(offset + i + 1), lc(locals, i));

            wl(out, deep, "        ), java.util.LinkedHashMap::new, Collectors.toList()");
            wl(out, deep, "    )");
            wl(out, deep, ").entrySet().stream()");
            wl(out, deep, ".filter(g", String.valueOf(level), " ->");

            for (var i = 0; i < locals.size(); i++)
                wl(
                    out, deep + 4, "java.util.Objects.nonNull(g",
                    String.valueOf(level), ".getKey().c",
                    String.valueOf(offset + i + 1), ") ", la(locals, i)
                );

            wl(out, deep, ").map(g", String.valueOf(level), " ->");
            wl(out, deep, "    new ", dtoJavaClassName, "(");

            for (var i = 0; i < locals.size(); i++) {
                w(
                    out, deep + 8, "g", String.valueOf(level),
                    ".getKey().c", String.valueOf(offset + i + 1)
                );
                wl(out, 0, ",");
            }

            var baseOffset = offset + locals.size();
            for (var i = 0; i < next.size(); i++) {
                var n = next.get(i);

                w(out, deep + 8, "g", String.valueOf(level), ".getValue().stream()");
                nextOperations(
                    level + 1, baseOffset, deep + 8, extraDto, out,
                    ((AggregatedType) n.type()).javaClassName(),
                    ((AggregatedType) n.type()).fields()
                );
                wl(out, 0, ".toList()", lc(next, i));

                baseOffset += ((AggregatedType) n.type()).fields().size();
            }

            wl(out, deep, "    )");
            w(out, deep, ")");
        }
    }

    public static String generate(
        String dtoJavaClassName, List<Field> fields,
        BiFunction<String, Integer, String> typeToGetField
    ) {
        var hasGrouping = fields.stream()
            .anyMatch(f -> f.type() instanceof AggregatedType);

        var out = new StringBuilder();
        wl(
            out, 0, "static java.util.stream.Stream<",
            dtoJavaClassName, "> doTheMapping(java.sql.ResultSet rs) {"
        );

        var flattened = flattenStartRow(fields);
        var nextOut = new StringBuilder();
        final String baseDto;

        if (hasGrouping) {
            var extraDto = new StringBuilder();
            nextOperations(1, 0, 4, extraDto, nextOut, dtoJavaClassName, fields);

            wl(out, 4, "record Row(");

            for (var i = 0; i < flattened.size(); i++)
                wl(
                    out, 8, flattened.get(i).javaClassName(),
                    " c", String.valueOf(i + 1), lc(flattened, i)
                );

            wl(out, 4, ") {}");
            out.append(extraDto);
            wl(out, 0);

            baseDto = "Row";
        } else
            baseDto = dtoJavaClassName;

        wl(out, 4, "return java.util.stream.Stream.iterate(");
        wl(out, 4, "    rs, t -> {");
        wl(out, 4, "        try {");
        wl(out, 4, "            return t.next();");
        wl(out, 4, "        } catch (java.sql.SQLException e) {");
        wl(out, 4, "            throw new RuntimeException(e);");
        wl(out, 4, "        }");
        wl(out, 4, "    }, t -> t");
        wl(out, 4, ").map(t -> {");
        wl(out, 4, "    try {");
        wl(out, 4, "        return new ", baseDto, "(");

        for (var i = 0; i < flattened.size(); i++)
            wl(
                out, 16,
                typeToGetField.apply(flattened.get(i).javaClassName(), i + 1),
                lc(flattened, i)
            );

        wl(out, 4, "        );");
        wl(out, 4, "    } catch(java.sql.SQLException ex) {");
        wl(out, 4, "        throw new RuntimeException(ex);");
        wl(out, 4, "    }");

        w(out, 4, "})");
        out.append(nextOut);
        w(out, 0, ";");
        wl(out, 0, "\n}");

        return out.toString();
    }
}
