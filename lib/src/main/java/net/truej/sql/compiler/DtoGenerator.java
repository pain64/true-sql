package net.truej.sql.compiler;

import java.util.function.Function;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.compiler.StatementGenerator.Out.each;

public class DtoGenerator {

    public static void generate(Out out, AggregatedType forType) {
        var declType = (Function<Field, String>) f -> switch (f.type()) {
            case AggregatedType _ -> f.type().javaClassName().startsWith("List<")
                ? f.type().javaClassName()
                : STR."List<\{f.type().javaClassName()}>";
            case ScalarType _ -> f.type().javaClassName();
        };

        var nestedTypes = forType.fields().stream()
            .filter(f ->
                f.type() instanceof AggregatedType at &&
                !at.javaClassName().startsWith("List<")
            )
            .map(f -> (AggregatedType) f.type())
            .toList();

        var nestedDto = each(nestedTypes, "\n", (o, _, at) -> {
            generate(o, at);
            return null;
        });

        var fieldDefinitions = each(forType.fields(), "\n", (o, _, f) -> {
            var nullability = f.type() instanceof ScalarType st ?
                switch (st.nullMode()) {
                    case EXACTLY_NULLABLE -> "@Nullable ";
                    case DEFAULT_NOT_NULL -> "";
                    case EXACTLY_NOT_NULL -> "@NotNull ";
                } : "";

            return ((Out) o)."\{nullability}public final \{declType.apply(f)} \{f.name()};";
        });

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
            public static class \{forType.javaClassName()} {
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