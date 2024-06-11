package com.truej.sql.v3.compiler;

import java.util.List;
import java.util.stream.Collectors;

import static com.truej.sql.v3.compiler.GLangParser.*;

public class DtoGenerator {
    // FIXME: recursive case for Non-scalar version ???
    // FIXME: remove .type().javaClassName()
    public static String generateDto(String javaClassName, List<Field> fields) {

        var fieldDefinitions = fields.stream()
            .map(f -> STR."public final \{f.type().javaClassName()} \{f.name()};")
            .collect(Collectors.joining("\n    "));

        var constructorParameters = fields.stream()
            .map(f -> STR."\{f.type().javaClassName()} \{f.name()}")
            .collect(Collectors.joining(",\n        "));

        var constructorFieldAssignments = fields.stream()
            .map(f -> STR."this.\{f.name()} = \{f.name()};")
            .collect(Collectors.joining("\n        "));

        var equalsFieldComparisons = fields.stream()
            .map(f -> STR."java.util.Objects.equals(this.\{f.name()}, o.\{f.name()})")
            .collect(Collectors.joining(" &&\n            "));

        var hashCodeCalculations = fields.stream()
            .map(f ->
                STR."h = h * 59 + java.util.Objects.hashCode(this.\{f.name()});"
            )
            .collect(Collectors.joining("\n        "));

        return STR."""
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
    }
}