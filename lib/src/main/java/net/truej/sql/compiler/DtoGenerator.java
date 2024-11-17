package net.truej.sql.compiler;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.compiler.StatementGenerator.Out.each;

public class DtoGenerator {

    private static String javaClassNameForField(Field field) {
        return switch (field) {
            case Aggregated ag -> "List<" + switch (ag) {
                case ListOfGroupField lgf -> lgf.newJavaClassName();
                case ListOfScalarField lsf -> lsf.binding().className();
            } + ">";
            case ScalarField sf -> sf.binding().className();
        };
    }

    public static void generate(Out out, ListOfGroupField forGroup) {

        var nestedTypes = forGroup.fields().stream()
            .filter(f -> f instanceof ListOfGroupField)
            .map(f -> (ListOfGroupField) f)
            .toList();

        var nestedDto = each(nestedTypes, "\n", (o, _, at) -> {
            generate(o, at);
            return null;
        });

        var fieldDefinitions = each(forGroup.fields(), "\n", (o, _, f) -> {
            var nullability = f instanceof ScalarField st ?
                switch (st.nullMode()) {
                    case EXACTLY_NULLABLE -> "@Nullable ";
                    case DEFAULT_NOT_NULL -> "";
                    case EXACTLY_NOT_NULL -> "@NotNull ";
                } : "";

            return ((Out) o)."\{nullability}public final \{javaClassNameForField(f)} \{f.name()};";
        });

        var constructorParameters = each(forGroup.fields(), ",\n", (o, _, f) ->
            o."\{javaClassNameForField(f)} \{f.name()}"
        );

        var constructorFieldAssignments = each(forGroup.fields(), "\n", (o, _, f) ->
            o."this.\{f.name()} = \{f.name()};"
        );

        var equalsFieldComparisons = each(forGroup.fields(), " &&\n", (o, _, f) ->
            o."java.util.Objects.equals(this.\{f.name()}, o.\{f.name()})"
        );

        var hashCodeCalculations = each(forGroup.fields(), "\n", (o, _, f) ->
            o."h = h * 59 + java.util.Objects.hashCode(this.\{f.name()});"
        );

        var _ = out."""
            \{nestedDto}
            public static class \{forGroup.newJavaClassName()} {
                \{fieldDefinitions}

                public \{forGroup.newJavaClassName()}(
                    \{constructorParameters}
                ) {
                    \{constructorFieldAssignments}
                }

                @Override public boolean equals(Object other) {
                    return this == other || (
                        other instanceof \{forGroup.newJavaClassName()} o &&
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