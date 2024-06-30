package com.truej.sql.v3.compiler;

import java.util.List;
import java.util.function.Function;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.StatementGenerator.*;
import static com.truej.sql.v3.compiler.StatementGenerator.Out.each;

public class DtoGenerator {
    // FIXME: recursive case for Non-scalar version ???
    // FIXME: remove .type().javaClassName()

    // FIXME: generate nested
//    record BBB(Bill b) {
//        record Bill() {}
//    }
//
//    record AAA(Bill b) {
//        record Bill() {}
//    }
    public static void generate(Out out, AggregatedType forType) {
        var declType = (Function<Field, String>) f -> switch (f.type()) {
            case AggregatedType _ -> STR."List<\{f.type().javaClassName()}>";
            case ScalarType _ -> f.type().javaClassName();
        };

        var nestedTypes = forType.fields().stream()
            .filter(f -> f.type() instanceof AggregatedType)
            .map(f -> (AggregatedType) f.type())
            .toList();

        var nestedDto = each(nestedTypes, "\n", (o, _, at) -> {
            generate(o, at);
            return null;
        });

        var fieldDefinitions = each(forType.fields(), "\n", (o, _, f) ->
            o."public final \{declType.apply(f)} \{f.name()};"
        );

        var constructorParameters = each(forType.fields(), ",\n", (o, _, f) ->
            o."\{declType.apply(f)} \{f.name()}"
        );

        var constructorFieldAssignments = each(forType.fields(), "\n", (o, _, f) ->
            o."this.\{f.name()} = \{f.name()};"
        );

        var equalsFieldComparisons = each(forType.fields(), " &&\n", (o, _, f) ->
            o."java.util.Objects.equals(this.\{f.name()}, o.\{f.name()})"
        );

        var hashCodeCalculations = each(forType.fields(), "\n", (o, _, f) ->
            o."h = h * 59 + java.util.Objects.hashCode(this.\{f.name()});"
        );

        var _ = out."""
            \{nestedDto}
            class \{forType.javaClassName()} {
                \{fieldDefinitions}

                public \{forType.javaClassName()}(
                    \{constructorParameters}
                ) {
                    \{constructorFieldAssignments}
                }

                @Override public boolean equals(Object other) {
                    return this == other || (
                        other instanceof \{forType.javaClassName()} o &&
                        \{equalsFieldComparisons}
                    );
                }

                @Override public int hashCode() {
                    int h = 1;
                    \{hashCodeCalculations}
                    return h;
                }
            }""";
    }
}