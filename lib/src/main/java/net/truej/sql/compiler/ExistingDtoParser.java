package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.truej.sql.compiler.GLangParser.*;

public class ExistingDtoParser {
    // FIXME: g and Nullable or NotNull annotation for dtoType
    //       is not compatible!!!

    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }

    public static FieldType parse(
        NullMode artumentNullMode,
        Function<String, Boolean> hasTypeBinding,
        Name initName, // constructor method name
        Symbol.ClassSymbol dtoSymbol
    ) {

        if (dtoSymbol.isInterface())
            throw new ParseException("To Dto class cannot be interface");

        if (!dtoSymbol.getTypeParameters().isEmpty())
            throw new ParseException("To Dto class cannot be generic");

        if (hasTypeBinding.apply(dtoSymbol.className())) {
            final NullMode nullMode;

            if (dtoSymbol.type.isPrimitive()) {
                if (artumentNullMode == NullMode.EXACTLY_NULLABLE)
                    throw new ParseException(
                        "result of primitive type cannot be marked as Nullable"
                    );

                if (artumentNullMode == NullMode.EXACTLY_NOT_NULL)
                    throw new ParseException(
                        "result of primitive type not needed to be marked as NotNull"
                    );
                nullMode = NullMode.EXACTLY_NOT_NULL;
            } else
                nullMode = artumentNullMode;

            return new ScalarType(nullMode, dtoSymbol.className());
        } else {
            if (artumentNullMode != NullMode.DEFAULT_NOT_NULL)
                throw new ParseException(
                    "Nullable or NotNull hint not allowed for aggregated DTO"
                );

            var allConstructors = dtoSymbol.members().getSymbols(s ->
                s instanceof Symbol.MethodSymbol cons &&
                cons.name.equals(initName) && cons.params.nonEmpty()
            ).iterator();

            if (!allConstructors.hasNext())
                throw new ParseException("non-empty args constructor not found");

            var constructor = (Symbol.MethodSymbol) allConstructors.next();

            if (allConstructors.hasNext())
                throw new ParseException("has more then one non-empty args constructor");

            var fields = constructor.params.stream().map(p -> {
                if (p.type.tsym instanceof Symbol.ClassSymbol cSym) {
                    var className = cSym.className();

                    if (className.equals(List.class.getName())) {
                        var parsed = parse(
                            NullMode.DEFAULT_NOT_NULL,
                            hasTypeBinding,
                            initName,
                            (Symbol.ClassSymbol) p.type.allparams().head.tsym
                        );
                        return new Field(
                            parsed instanceof ScalarType st ? new AggregatedType(
                                "List<" + st.javaClassName() + ">",
                                List.of(new Field(st, ""))
                            ) : parsed,
                            p.name.toString()
                        );
                    } else if (hasTypeBinding.apply(className)) {
                        var isMarkedAsNullable = p.getAnnotation(Nullable.class) != null;
                        var isMarkedAsNotNull = p.getAnnotation(NotNull.class) != null;

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

                        return new Field(
                            new ScalarType(fieldNullMode, className), p.name.toString()
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

            return new AggregatedType(dtoSymbol.className(), fields);
        }
    }


    public sealed interface EntryName { }
    public record ReturnValue() implements EntryName { }
    public record FieldName(String name) implements EntryName { }
    public record FlattenedDtoEntry(String name, ScalarType st) { }

    private static List<FlattenedDtoEntry> flattenAggregatedType(AggregatedType at) {
        return at.fields().stream().flatMap(f ->
            switch (f.type()) {
                case ScalarType t -> Stream.of(new FlattenedDtoEntry(f.name(), t));
                case AggregatedType t -> flattenAggregatedType(t).stream();
            }
        ).toList();
    }

    public static List<FlattenedDtoEntry> flattedDtoType(FieldType ft) {
        return switch (ft) {
            case ScalarType st -> List.of(
                new FlattenedDtoEntry("result", st)
            );
            case AggregatedType at -> flattenAggregatedType(at);
        };
    }
}
