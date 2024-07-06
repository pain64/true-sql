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

    public static FieldType parse(
        NullMode nullMode,
        Function<String, Boolean> hasTypeBinding,
        Name initName, // constructor method name
        Symbol.ClassSymbol dtoSymbol
    ) {

        if (hasTypeBinding.apply(dtoSymbol.className()))
            return new ScalarType(nullMode, dtoSymbol.className());
        else {
            if (nullMode != NullMode.DEFAULT_NOT_NULL)
                throw new RuntimeException(
                    "Nullable or NotNull hint not allowed for aggregated DTO"
                );

            var allConstructors = dtoSymbol.members().getSymbols(s ->
                s instanceof Symbol.MethodSymbol cons &&
                cons.name.equals(initName) && cons.params.nonEmpty()
            ).iterator();

            if (!allConstructors.hasNext())
                throw new RuntimeException("non-empty args constructor not found");

            var constructor = (Symbol.MethodSymbol) allConstructors.next();

            if (allConstructors.hasNext())
                throw new RuntimeException("has more then one non-empty args constructor");

            var fields = constructor.params.stream().map(p -> {
                if (p.type.tsym instanceof Symbol.ClassSymbol cSym) {
                    var className = cSym.className();

                    if (className.equals("java.util.List")) {
                        return new Field(
                            parse(
                                NullMode.DEFAULT_NOT_NULL,
                                hasTypeBinding,
                                initName,
                                (Symbol.ClassSymbol) p.type.allparams().head.tsym
                            ),
                            p.name.toString()
                        );
                    } else if (hasTypeBinding.apply(className)) {
                        var fieldNullMode = p.getAnnotation(Nullable.class) != null
                            ? NullMode.EXACTLY_NULLABLE :
                            (
                                p.getAnnotation(NotNull.class) != null
                                    ? NullMode.EXACTLY_NOT_NULL
                                    : NullMode.DEFAULT_NOT_NULL
                            );

                        return new Field(
                            new ScalarType(fieldNullMode, className), p.name.toString()
                        );
                    } else
                        throw new RuntimeException(
                            "has no type binding for " + className
                        );
                } else
                    throw new RuntimeException(
                        "unexpected constructor parameter of kind: " + p.type.tsym.kind
                    );
            }).toList();

            return new AggregatedType(dtoSymbol.className(), fields);
        }
    }

    public record FlattenedDtoField(@Nullable String name, ScalarType st) { }

    private static List<FlattenedDtoField> flattenAggregatedType(AggregatedType at) {
        return at.fields().stream().flatMap(f ->
            switch (f.type()) {
                case ScalarType t -> Stream.of(new FlattenedDtoField(f.name(), t));
                case AggregatedType t -> flattenAggregatedType(t).stream();
            }
        ).toList();
    }

    public static List<FlattenedDtoField> flattedDtoType(FieldType ft) {
        return switch (ft) {
            case ScalarType st -> List.of(
                new ExistingDtoParser.FlattenedDtoField(null, st)
            );
            case AggregatedType at -> flattenAggregatedType(at);
        };
    }
}
