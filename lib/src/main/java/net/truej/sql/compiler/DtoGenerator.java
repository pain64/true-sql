package net.truej.sql.compiler;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.compiler.StatementGenerator.Out.each;
import static net.truej.sql.compiler.TrueSqlPlugin.classNameToSourceCodeType;

public class DtoGenerator {

    private static String sourceCodeTypeForField(Field field) {
        return switch (field) {
            case Aggregated ag -> "List<" + switch (ag) {
                case ListOfGroupField lgf -> lgf.newJavaClassName();
                case ListOfScalarField lsf -> classNameToSourceCodeType(lsf.binding().className());
            } + ">";
            case ScalarField sf -> classNameToSourceCodeType(sf.binding().className());
        };
    }

    public static void generate(Out out, ListOfGroupField forGroup) {

        var nestedTypes = forGroup.fields().stream()
            .filter(f -> f instanceof ListOfGroupField)
            .map(f -> (ListOfGroupField) f)
            .toList();

        var nestedDto = each(nestedTypes, "\n", (o, __, at) -> {
            generate(o, at);
            return null;
        });

        var fieldDefinitions = each(forGroup.fields(), "\n", (o, __, f) -> {
            var nullability = f instanceof ScalarField st ?
                switch (st.nullMode()) {
                    case EXACTLY_NULLABLE -> "@Nullable ";
                    case DEFAULT_NOT_NULL -> "";
                    case EXACTLY_NOT_NULL -> "@NotNull ";
                } : "";

            return o.w(
                nullability, "public final ", sourceCodeTypeForField(f), " ", f.name(), ";"
            );
        });

        var constructorParameters = each(forGroup.fields(), ",\n", (o, __, f) ->
            o.w(sourceCodeTypeForField(f), " ", f.name())
        );

        var constructorFieldAssignments = each(forGroup.fields(), "\n", (o, __, f) ->
            o.w("this.", f.name(), " = ", f.name(), ";")
        );

        var equalsFieldComparisons = each(forGroup.fields(), " &&\n", (o, __, f) ->
            o.w("java.util.Objects.equals(this.", f.name(), ", o.", f.name(), ")")
        );

        var hashCodeCalculations = each(forGroup.fields(), "\n", (o, __, f) ->
            o.w("h = h * 59 + java.util.Objects.hashCode(this.", f.name(), ");")
        );

        out.w(
            nestedDto, "\n",
            "public static class ", forGroup.newJavaClassName(), " {\n",
            "    ", fieldDefinitions, "\n",
            "\n",
            "    public ", forGroup.newJavaClassName(), "(\n",
            "        ", constructorParameters, "\n",
            "    ) {\n",
            "        ", constructorFieldAssignments, "\n",
            "    }",
            "\n",
            "    @Override public boolean equals(Object other) {\n",
            "        return this == other || (\n",
            "            other instanceof ", forGroup.newJavaClassName(), " o &&\n",
            "            ", equalsFieldComparisons, "\n",
            "        );\n",
            "    }\n",
            "\n",
            "    @Override public int hashCode() {\n",
            "        int h = 1;\n",
            "        ", hashCodeCalculations, "\n",
            "        return h;\n",
            "    }\n",
            "}"
        );
    }
}