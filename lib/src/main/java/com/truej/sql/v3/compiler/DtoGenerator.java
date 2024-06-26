package com.truej.sql.v3.compiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.StatementGenerator.*;
import static com.truej.sql.v3.compiler.StatementGenerator.Out.each;
import static java.lang.StringTemplate.RAW;

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
    public static String generateDto(String javaClassName, List<Field> fields) {
        var out = new Out(new StringBuilder());

        var fieldDefinitions = each(fields, "", (o, _, f) ->
            o."public final \{f.type().javaClassName()} \{f.name()};"
        );

        var constructorParameters = each(fields, ",", (o, _, f) ->
            o."\{f.type().javaClassName()} \{f.name()}"
        );

        var constructorFieldAssignments = each(fields, "", (o, _, f) ->
            o."this.\{f.name()} = \{f.name()};"
        );

        var equalsFieldComparisons = each(fields, " &&", (o, _, f) ->
            o."java.util.Objects.equals(this.\{f.name()}, o.\{f.name()})"
        );

        var hashCodeCalculations = each(fields, "", (o, _, f) ->
            o."h = h * 59 + java.util.Objects.hashCode(this.\{f.name()});"
        );

        var _ = out."""
            class \{javaClassName} {
                \{fieldDefinitions}

                public \{javaClassName}(
                    \{constructorParameters}
                ) {
                    \{constructorFieldAssignments}
                }

                @Override public boolean equals(Object other) {
                    return this == other || (
                        other instanceof \{javaClassName} o &&
                        \{equalsFieldComparisons}
                    );
                }

                @Override public int hashCode() {
                    int h = 1;
                    \{hashCodeCalculations}
                    return h;
                }
            }
            """;

        return out.buffer.toString();
    }
}