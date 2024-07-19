package net.truej.sql.compiler;

import net.truej.sql.compiler.InvocationsFinder.ValidationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GLangParser {
    public sealed interface Lexeme { }
    public record Text(String t) implements Lexeme { }
    public record Colon() implements Lexeme { }
    public record Dot() implements Lexeme { }
    public sealed interface NullabilityMark extends Lexeme { }
    public record QuestionMark() implements NullabilityMark { }
    public record ExclamationMark() implements NullabilityMark { }
    public record End() implements Lexeme { }

    public static List<Lexeme> lex(String input) {
        var result = new ArrayList<Lexeme>();
        var acc = new StringBuilder(32);

        for (int i = 0; i < input.length(); i++) {
            var ch = input.charAt(i);

            var emitEscaped = (Consumer<Lexeme>) lexeme -> {
                if (acc.isEmpty()) {
                    if (lexeme != null) result.add(lexeme);
                } else {
                    if (acc.charAt(acc.length() - 1) == '\\')
                        acc.setCharAt(acc.length() - 1, ch);
                    else {
                        result.add(new Text(acc.toString()));
                        if (lexeme != null) result.add(lexeme);
                        acc.setLength(0);
                    }
                }
            };

            if (ch == '.')
                emitEscaped.accept(new Dot());
            else if (ch == ':')
                emitEscaped.accept(new Colon());
            else if (ch == '!')
                emitEscaped.accept(new ExclamationMark());
            else if (ch == '?')
                emitEscaped.accept(new QuestionMark());
            else if (Character.isWhitespace(ch))
                emitEscaped.accept(null);
            else
                acc.append(ch);
        }

        if (!acc.isEmpty())
            result.add(new Text(acc.toString()));

        result.add(new End());
        return result;
    }

    public record Chain(
        @Nullable String fieldClassName,
        @Nullable String fieldName,
        @Nullable Chain next
    ) { }
    public record Line(NullMode nullMode, @Nullable String javaClassName, Chain chain) { }

    private static NullMode markToNullMode(NullabilityMark m) {
        return switch (m) {
            case ExclamationMark _ -> NullMode.EXACTLY_NOT_NULL;
            case QuestionMark _ -> NullMode.EXACTLY_NULLABLE;
        };
    }

    public static Line parse(
        BindColumn bindColumn, ColumnMetadata column, int columnIndex, List<Lexeme> input
    ) {

        final NullMode nullMode;
        final @Nullable String javaClassNameHint;
        final int next;

        if (input.get(0) instanceof Colon) {
            if (!input.get(1).equals(new Text("t")))
                throw new ValidationException(STR."Expected t but has\{input.get(1)}");

            switch (input.get(2)) {
                case NullabilityMark m -> {
                    nullMode = markToNullMode(m);
                    javaClassNameHint = null;
                    next = 3;
                }
                case Text text -> {
                    javaClassNameHint = text.t;

                    if (input.get(3) instanceof NullabilityMark m) {
                        nullMode = markToNullMode(m);
                        next = 4;
                    } else {
                        nullMode = NullMode.DEFAULT_NOT_NULL;
                        next = 3;
                    }
                }
                default -> throw new ValidationException(
                    STR."Expected TEXT or QUESTION_MARK or EXCLAMATION_MARK but has \{input.get(2)}"
                );
            }
        } else {
            nullMode = NullMode.DEFAULT_NOT_NULL;
            javaClassNameHint = null;
            next = 0;
        }

        var bindResult = bindColumn.bind(column, columnIndex, javaClassNameHint, nullMode);

        return new Line(
            bindResult.nullMode, bindResult.javaClassName, parseChain(input, next)
        );
    }

    public static Chain parseChain(List<Lexeme> input, int i) {
        return switch (input.get(i)) {
            case End _ -> new Chain(null, null, null);
            case Text t1 -> switch (input.get(i + 1)) {
                case End _ -> new Chain(null, t1.t, null);
                case Dot _ -> new Chain(null, t1.t, parseChain(input, i + 2));
                case Text t2 -> switch (input.get(i + 2)) {
                    case End _ -> new Chain(t1.t, t2.t, null);
                    case Dot _ -> new Chain(t1.t, t2.t, parseChain(input, i + 3));
                    default -> throw new ValidationException(
                        STR."expected END or DOT but has \{input.get(i + 2)}"
                    );
                };
                default -> throw new ValidationException(
                    STR."expected END or DOT or TEXT but has \{input.get(i + 1)}"
                );
            };
            default -> throw new ValidationException(
                STR."expected END or TEXT but has \{input.get(i)}"
            );
        };
    }

    // FIXME: rename ???
    public sealed interface FieldType {
        String javaClassName();
    }
    public enum NullMode {EXACTLY_NULLABLE, DEFAULT_NOT_NULL, EXACTLY_NOT_NULL}

    public record ScalarType(
        NullMode nullMode, String javaClassName
    ) implements FieldType { }

    public record AggregatedType(
        String javaClassName,
        List<Field> fields
    ) implements FieldType { }

    public record Field(FieldType type, String name) { }

    public record NumberedColumn(int n, Line line) { }

    private static final Pattern SNAKE_TO_CAMEL_CASE_PATTERN = Pattern.compile("_(\\p{L})");

    private static String makeFieldName(String columnName) {
        return SNAKE_TO_CAMEL_CASE_PATTERN
            .matcher(columnName.toLowerCase())
            .replaceAll(m -> m.group(1).toUpperCase());
    }

    private static List<Field> buildGroup(List<NumberedColumn> lines) {
        // FIXME: check that at least one local ???
        var locals = lines.stream()
            .filter(nl -> nl.line.chain.next == null).toList();

        var baseNumber = locals.getFirst().n;
        for (var i = 0; i < locals.size(); i++) {
            if (baseNumber + i != locals.get(i).n)
                throw new ValidationException(
                    "The declarations of the members of the group should run consecutively"
                );
        }

        var localsChecked = locals.stream().map(nl -> {
            if (nl.line.chain.fieldName == null)
                throw new ValidationException("Field name required");

            if (nl.line.chain.fieldClassName != null)
                throw new ValidationException("Aggregated java class name not expected here");

            return new Field(
                new ScalarType(
                    nl.line.nullMode, nl.line.javaClassName
                ),
                makeFieldName(nl.line.chain.fieldName)
            );
        });

        var next = lines.stream()
            .filter(nl -> nl.line.chain.next != null)
            .collect(
                Collectors.groupingBy(
                    nl -> {
                        var javaFieldName = nl.line.chain.fieldName;
                        if (javaFieldName == null)
                            throw new ValidationException("Field name required");

                        return javaFieldName;
                    },
                    LinkedHashMap::new, Collectors.toList()
                )
            ).entrySet().stream().map(group -> {
                var groupLines = group.getValue();
                var groupFieldName = makeFieldName(group.getKey());

                if (groupLines.size() == 1) {
                    var numbered = groupLines.getFirst();
                    if (numbered.line.chain.fieldClassName != null)
                        throw new ValidationException(
                            "Dto class name not allowed for group with one element - " +
                            "thees groups converts to List<single group element class name> "
                        );

                    if (numbered.line.chain.next != null && numbered.line.chain.next.next != null)
                        throw new ValidationException(
                            "Inner groups not allowed for group with one element"
                        );

                    return new Field(
                        new AggregatedType(
                            "List<" + groupLines.getFirst().line.javaClassName + ">",
                            List.of(new Field(
                                new ScalarType(
                                    numbered.line.nullMode, numbered.line.javaClassName
                                ), ""
                            ))
                        ),
                        groupFieldName
                    );
                } else {
                    var aggregatedTypeName =
                        groupLines.getFirst().line.chain.fieldClassName;

                    if (aggregatedTypeName == null)
                        throw new ValidationException("Aggregated java class name required");

                    return new Field(
                        new AggregatedType(aggregatedTypeName, buildGroup(
                            groupLines.stream().map(nl -> new NumberedColumn(
                                nl.n, new Line(nl.line.nullMode, nl.line.javaClassName, nl.line.chain.next)
                            )).toList()
                        )),
                        groupFieldName
                    );
                }
            });

        return Stream.concat(localsChecked, next).toList();
    }

    public record ColumnMetadata(
        NullMode nullMode, int sqlType,
        String sqlTypeName, String javaClassName, String columnName
    ) { }

    public interface BindColumn {
        record Result(String javaClassName, NullMode nullMode) { }
        Result bind(
            ColumnMetadata column, int columnIndex, @Nullable String javaClassNameHint, NullMode dtoNullMode
        );
    }

    public static List<Field> parseResultSetColumns(
        List<ColumnMetadata> columns, BindColumn bindColumn
    ) {
        return buildGroup(
            IntStream
                .range(0, columns.size())
                .mapToObj(n -> {
                    var column = columns.get(n);
                    return new NumberedColumn(n, parse(bindColumn, column, n, lex(column.columnName)));
                })
                .toList()
        );
    }
}
