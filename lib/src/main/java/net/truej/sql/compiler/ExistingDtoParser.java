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
    // FIXME: g and Nullable or NotNull annotation for dtoType
    //       is not compatible!!!

    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }

    // FIXME: использовать отдельные структуры данных - не использовать те, которые для
    // генерации DTO
    public static Field parse(
        List<Standard.Binding> bindings,
        String toFieldName,
        NullMode argumentNullMode,
        Name initName, // constructor method name
        Type dtoType
    ) {
        // Удалить дубли в плане логики, запретить List<> на первом уровне

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
                        "result of primitive type cannot be marked as Nullable"
                    );

                if (argumentNullMode == NullMode.EXACTLY_NOT_NULL)
                    throw new ParseException(
                        "result of primitive type not needed to be marked as NotNull"
                    );
                nullMode = NullMode.EXACTLY_NOT_NULL;
            } else
                nullMode = argumentNullMode;

            return new ScalarField(toFieldName, nullMode, binding);
        } else {
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
                if (p.type.tsym instanceof Symbol.ClassSymbol clSym) {
                    var className = typeToClassName(p.type);

                    // FIXME: perf
                    if (clSym.flatname.toString().equals(List.class.getName()))

                        return switch (
                            parse(
                                bindings,
                                p.name.toString(),
                                NullMode.DEFAULT_NOT_NULL,
                                initName,
                                p.type.allparams().head
                            )
                            ) {

                            // x
                            // Y y.a
                            //   y.b
                            //   z.
                            // x
                            // yA
                            // yB
                            // Scalar: Aggregatable - то что биндится напрямую
                            // Dto: Aggregatable - одно или несколько полей
                            // Aggregation: (name, List<Aggregatable>)
                            case ListOfScalarField __ ->
                                throw new ParseException("forbidden List<Lust<...");
                            case ListOfGroupField lgf -> (Field) lgf;
                            case ScalarField sf -> (Field) new ListOfScalarField(
                                sf.name(), sf.nullMode(), sf.binding()
                            );
                        };
                        // FIXME: this logic is duplicated???
                    else if (
                        bindings.stream()
                            .anyMatch(b -> b.className().equals(className))
                    ) {
                        var isMarkedAsNullable = p.getAnnotation(Nullable.class) != null;
                        var isMarkedAsNotNull = p.getAnnotation(NotNull.class) != null;

                        // FIXME: deduplicate this logic
                        var binding2 = bindings.stream()
                            .filter(b -> b.className().equals(className))
                            .findFirst().orElseThrow();

                        if (p.type.isPrimitive()) {
                            if (isMarkedAsNullable) throw new ParseException(
                                "Dto field of primitive type cannot be marked as @Nullable"
                            );

                            if (isMarkedAsNotNull) throw new ParseException(
                                "Dto field of primitive type not needed to be marked as @NotNull"
                            );
                        }

                        var fieldNullMode = p.type.isPrimitive() ? NullMode.EXACTLY_NOT_NULL :
                            isMarkedAsNullable ? NullMode.EXACTLY_NULLABLE : (
                                isMarkedAsNotNull
                                    ? NullMode.EXACTLY_NOT_NULL
                                    : NullMode.DEFAULT_NOT_NULL
                            );

                        return new ScalarField(
                            p.name.toString(), fieldNullMode, binding2
                        );
                    } else
                        throw new ParseException(
                            "has no type binding for " + className
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
