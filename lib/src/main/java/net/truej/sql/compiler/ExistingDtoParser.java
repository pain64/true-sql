package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Name;
import net.truej.sql.bindings.Standard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.TrueSqlPlugin.typeToClassName;

public class ExistingDtoParser {

    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }

    static Field parse(
        List<Standard.Binding> bindings,
        boolean isEntry,
        boolean isToList,
        String toFieldName,
        NullMode argumentNullMode,
        Name initName, // constructor method name
        Type dtoType
    ) {

        if (dtoType.tsym.flatName().contentEquals(List.class.getName())) {
            if (dtoType.getTypeArguments().isEmpty())
                throw new ParseException("Raw list cannot be used as Dto or Dto field");

            if (isToList)
                throw new ParseException("value of List<List<...>> cannot be Dto field");

            return parse(
                bindings, false, true, toFieldName, argumentNullMode, initName,
                dtoType.getTypeArguments().getFirst()
            );
        }

        if (dtoType.isInterface())
            throw new ParseException("To Dto class cannot be interface");

        if (!dtoType.tsym.getTypeParameters().isEmpty())
            throw new ParseException("To Dto class cannot be generic");

        if (
            !dtoType.isPrimitive() &&
            dtoType.tsym instanceof Symbol.ClassSymbol cl &&
            cl.isInner() && !cl.isStatic()
        )
            throw new ParseException("To Dto class must be static if inner");

        var binding = bindings.stream()
            .filter(b -> b.className().equals(typeToClassName(dtoType)))
            .findFirst().orElse(null);

        if (binding != null) {
            final NullMode nullMode;

            if (dtoType.isPrimitive()) {
                if (argumentNullMode == NullMode.EXACTLY_NULLABLE)
                    throw new ParseException(
                        isEntry
                            ? "result of primitive type cannot be marked as Nullable"
                            : "Dto field of primitive type cannot be marked as @Nullable"
                    );

                if (argumentNullMode == NullMode.EXACTLY_NOT_NULL)
                    throw new ParseException(
                        isEntry
                            ? "result of primitive type not needed to be marked as NotNull"
                            : "Dto field of primitive type not needed to be marked as @NotNull"
                    );
                nullMode = NullMode.EXACTLY_NOT_NULL;
            } else
                nullMode = argumentNullMode;

            return isToList
                ? new ListOfScalarField(toFieldName, nullMode, binding)
                : new ScalarField(toFieldName, nullMode, binding);
        } else {

            if (dtoType.isPrimitive() || !isEntry && !isToList)
                throw new ParseException(
                    "has no type binding for " + typeToClassName(dtoType)
                );

            if (argumentNullMode != NullMode.DEFAULT_NOT_NULL)
                throw new ParseException(
                    "Nullable or NotNull hint not allowed for aggregated DTO"
                );

            var allConstructors = dtoType.tsym.members().getSymbols(s ->
                s instanceof Symbol.MethodSymbol cons &&
                cons.name.equals(initName) && cons.params.nonEmpty()
            ).iterator();

            if (!allConstructors.hasNext())
                throw new ParseException("non-empty args constructor not found");

            var constructor = (Symbol.MethodSymbol) allConstructors.next();

            if (allConstructors.hasNext())
                throw new ParseException("has more than one non-empty args constructor");

            var fields = constructor.params.stream().map(p -> {
                if (p.type.tsym instanceof Symbol.ClassSymbol) {
                    var isMarkedAsNullable = p.getAnnotation(Nullable.class) != null;
                    var isMarkedAsNotNull = p.getAnnotation(NotNull.class) != null;

                    return parse(
                        bindings, false, false, p.name.toString(),
                        isMarkedAsNullable ? NullMode.EXACTLY_NULLABLE : (
                            isMarkedAsNotNull
                                ? NullMode.EXACTLY_NOT_NULL
                                : NullMode.DEFAULT_NOT_NULL
                        ), initName, p.type
                    );
                } else
                    throw new ParseException(
                        "unexpected constructor parameter of kind: " + p.type.tsym.kind
                    );
            }).toList();

            return new ListOfGroupField(toFieldName, dtoType.toString(), fields);
        }
    }

    public static List<ScalarField> flattenedDtoType(Field field) {
        return switch (field) {
            case ScalarField sf -> List.of(sf);
            case ListOfGroupField lgf ->
                lgf.fields().stream().flatMap(f -> flattenedDtoType(f).stream()).toList();
            case ListOfScalarField lsf ->
                List.of(new ScalarField(lsf.name(), lsf.nullMode(), lsf.binding()));
        };
    }
}
