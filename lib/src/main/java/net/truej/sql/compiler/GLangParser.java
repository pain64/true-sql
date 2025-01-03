package net.truej.sql.compiler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.truej.sql.bindings.Standard.*;

public class GLangParser {

    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }

    public sealed interface Lexeme { }
    public record Text(String t) implements Lexeme {
        @Override public String toString() { return "TEXT(" + t + ")"; }
    }
    public record Colon() implements Lexeme {
        @Override public String toString() { return "COLON"; }
    }
    public record Dot() implements Lexeme {
        @Override public String toString() { return "DOT"; }
    }

    public sealed interface NullabilityMark extends Lexeme { }
    public record QuestionMark() implements NullabilityMark {
        @Override public String toString() { return "QUESTION_MARK"; }
    }
    public record ExclamationMark() implements NullabilityMark {
        @Override public String toString() { return "EXCLAMATION_MARK"; }
    }

    public record End() implements Lexeme {
        @Override public String toString() { return "END"; }
    }

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
        @Nullable String groupClassName,
        @Nullable String fieldName,
        @Nullable Chain next
    ) { }
    public record Line(NullMode nullMode, @Nullable String javaClassNameHint, Chain chain) { }

    private static NullMode markToNullMode(NullabilityMark m) {
        return switch (m) {
            case ExclamationMark __ -> NullMode.EXACTLY_NOT_NULL;
            case QuestionMark __ -> NullMode.EXACTLY_NULLABLE;
        };
    }

    public static Line parseLine(
        // BindColumn bindColumn, ColumnMetadata column, int columnIndex,
        List<Lexeme> input
    ) {

        final NullMode nullMode;
        final @Nullable String javaClassNameHint;
        final int next;

        if (input.get(0) instanceof Colon) {
            if (!input.get(1).equals(new Text("t")))
                throw new ParseException("Expected t but has " + input.get(1));

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
                default -> throw new ParseException(
                    "Expected TEXT or QUESTION_MARK or EXCLAMATION_MARK but has " + input.get(2)
                );
            }
        } else {
            nullMode = NullMode.DEFAULT_NOT_NULL;
            javaClassNameHint = null;
            next = 0;
        }

        //var bindResult = bindColumn.bind(column, columnIndex, javaClassNameHint, nullMode);

        return new Line(
            nullMode, javaClassNameHint, parseChain(input, next)
        );
    }

    public static Chain parseChain(List<Lexeme> input, int i) {
        return switch (input.get(i)) {
            case End __ -> new Chain(null, null, null);
            case Text t1 -> switch (input.get(i + 1)) {
                case End __ -> new Chain(null, t1.t, null);
                case Dot __ -> new Chain(null, t1.t, parseChain(input, i + 2));
                case Text t2 -> switch (input.get(i + 2)) {
                    case End __ -> new Chain(t1.t, t2.t, null);
                    case Dot __ -> new Chain(t1.t, t2.t, parseChain(input, i + 3));
                    default -> throw new ParseException(
                        "expected END or DOT but has " + input.get(i + 2)
                    );
                };
                default -> throw new ParseException(
                    "expected END or DOT or TEXT but has " + input.get(i + 1)
                );
            };
            default -> throw new ParseException(
                "expected END or TEXT but has " + input.get(i)
            );
        };
    }

    public enum NullMode {EXACTLY_NULLABLE, DEFAULT_NOT_NULL, EXACTLY_NOT_NULL}

    public sealed interface Field {
        String name();
    }
    public sealed interface FetchToField extends Field { }
    sealed interface Aggregated { }

    public record ScalarField(
        String name, NullMode nullMode, Binding binding
    ) implements FetchToField { }

    public record ListOfScalarField(
        // FIXME: разве колонка после агрегации может быть null???
        String name, NullMode nullMode, Binding binding
    ) implements Field, Aggregated { }

    public record ListOfGroupField(
        String name, String newJavaClassName, List<Field> fields
    ) implements FetchToField, Aggregated { }

    public record NumberedColumn(int n, ColumnMetadata column, Line line) { }

    private static final Pattern SNAKE_TO_CAMEL_CASE_PATTERN = Pattern.compile("_(\\p{L})");

    private static String makeFieldName(String columnName) {
        return SNAKE_TO_CAMEL_CASE_PATTERN
            .matcher(columnName)
            .replaceAll(m -> m.group(1).toUpperCase());
    }

    private static List<Field> buildGroup(BindColumn bindColumn, List<NumberedColumn> lines) {
        // FIXME: check that at least one local ???
        var locals = lines.stream()
            .filter(nl -> nl.line.chain.next == null).toList();

        var baseNumber = locals.getFirst().n;
        for (var i = 0; i < locals.size(); i++) {
            if (baseNumber + i != locals.get(i).n)
                throw new ParseException(
                    "The declarations of the members of the group should run consecutively"
                );
        }

        var localsChecked = locals.stream().map(nl -> {
            if (nl.line.chain.fieldName == null)
                if (nl.column.columnName == null)
                    throw new ParseException(
                        "Your database driver doest not provides column name" +
                        " (labels only). Field name required"
                    );


            if (nl.line.chain.groupClassName != null)
                throw new ParseException("Aggregated java class name not expected here");

            var realFieldName = nl.line.chain.fieldName != null
                ? nl.line.chain.fieldName : nl.column.columnName;

            var bound = bindColumn.bind(
                nl.column, nl.n, nl.line.javaClassNameHint, nl.line.nullMode
            );

            return (Field) new ScalarField(
                makeFieldName(realFieldName), bound.nullMode, bound.binding
            );
        });

        var next = lines.stream()
            .filter(nl -> nl.line.chain.next != null)
            .collect(
                Collectors.groupingBy(
                    nl -> nl.line.chain.fieldName,
                    LinkedHashMap::new, Collectors.toList()
                )
            ).entrySet().stream().map(group -> {
                var groupLines = group.getValue();
                var groupFieldName = makeFieldName(group.getKey());

                if (groupLines.size() == 1) {
                    var numbered = groupLines.getFirst();
                    if (numbered.line.chain.groupClassName != null)
                        throw new ParseException(
                            "Dto class name not allowed for group with one element - " +
                            "thees groups converts to List<single group element class name>"
                        );

                    if (numbered.line.chain.next != null && numbered.line.chain.next.next != null)
                        throw new ParseException(
                            "Inner groups not allowed for group with one element"
                        );

                    var bound = bindColumn.bind(
                        numbered.column, numbered.n,
                        numbered.line.javaClassNameHint, numbered.line.nullMode
                    );

                    return new ListOfScalarField(
                        groupFieldName, bound.nullMode, bound.binding
                    );
                } else {
                    var groupClassName =
                        groupLines.getFirst().line.chain.groupClassName;

                    if (groupClassName == null)
                        throw new ParseException("Aggregated java class name required");

                    return new ListOfGroupField(
                        groupFieldName, groupClassName,
                        buildGroup(
                            bindColumn, groupLines.stream().map(nl ->
                                new NumberedColumn(
                                    nl.n, nl.column,
                                    new Line(
                                        nl.line.nullMode,
                                        nl.line.javaClassNameHint,
                                        nl.line.chain.next
                                    )
                                )
                            ).toList()
                        )
                    );
                }
            });

        return Stream.concat(localsChecked, next).toList();
    }

    // FIXME: remove this "abstraction"
    public record ColumnMetadata(
        NullMode nullMode, int sqlType,
        String sqlTypeName, String javaClassName,
        @Nullable String columnName, String columnLabel,
        int scale, int precision
    ) { }

    // FIXME: pass index-only. No ColumnMetadata!
    public interface BindColumn {
        record Result(Binding binding, NullMode nullMode) { }
        Result bind(
            ColumnMetadata column, int columnIndex, @Nullable String javaClassNameHint, NullMode nullModeHint
        );
    }

    public static List<Field> parseResultSetColumns(
        List<ColumnMetadata> columns, BindColumn bindColumn
    ) {
        return buildGroup(
            bindColumn, IntStream
                .range(0, columns.size())
                .mapToObj(n -> {
                    var column = columns.get(n);
                    return new NumberedColumn(n, column, parseLine(lex(column.columnLabel)));
                }).toList()
        );
    }
}
