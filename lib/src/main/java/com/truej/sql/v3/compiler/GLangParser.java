package com.truej.sql.v3.compiler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GLangParser {
    public sealed interface Lexeme { }
    public record Text(String t) implements Lexeme { }
    public record Colon() implements Lexeme { }
    public record Dot() implements Lexeme { }
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
        @Nullable String javaTypeName,
        @Nullable String javaFieldName,
        @Nullable Chain next
    ) { }
    public record Line(@Nullable String sqlTypeName, Chain chain) { }

    public static Line parse(List<Lexeme> input) {
        if (input.get(0) instanceof Colon) {
            if (!input.get(1).equals(new Text("t")))
                throw new RuntimeException(STR."Expected t but has\{input.get(1)}");

            if (!(input.get(2) instanceof Text))
                throw new RuntimeException(STR."Expected TEXT but has \{input.get(2)}");

            var sqlTypeName = ((Text) input.get(2)).t;

            return new Line(sqlTypeName, parseChain(input, 3));
        } else
            return new Line(null, parseChain(input, 0));
    }

    // inferred: i - Int
    // parseChain = List Lexeme :input, Int :i ->
    //     get input i ?
    //        End     -> Chain nil nil nil
    //        Text t1 -> get input i + 1 ?
    //            End -> Chain nil t1.t nil
    //            Dot -> Chain nil t1.t (parseChain input i + 2)
    //            Text t2 -> get input i + 2 ?
    //                End  -> Chain t1.t, t2.t, nil
    //                Dot  -> Chain t1.t, t2.t, (parseChain input i + 3)
    //                :l   -> E 'expected END or DOT but has {l}'
    //            :l -> E 'expected END or DOT or TEXT but has {l}'
    //         :l -> E 'expected END or TEXT but has {l}'

//    parseResultSetColumnNames =
//        List<String> columnNames, Function<String, String> sqlTypeToJava
//    ) {
//        return buildGroup(
//            IntStream
//                .range(0, columnNames.size())
//                .mapToObj(n -> new NumberedLine(n, parse(lex(columnNames.get(n)))))
//                .toList(),
//            sqlTypeToJava
//        );
//    }

    public static Chain parseChain(List<Lexeme> input, int i) {
        return switch (input.get(i)) {
            case End _ -> new Chain(null, null, null);
            case Text t1 -> switch (input.get(i + 1)) {
                case End _ -> new Chain(null, t1.t, null);
                case Dot _ -> new Chain(null, t1.t, parseChain(input, i + 2));
                case Text t2 -> switch (input.get(i + 2)) {
                    case End _ -> new Chain(t1.t, t2.t, null);
                    case Dot _ -> new Chain(t1.t, t2.t, parseChain(input, i + 3));
                    default -> throw new RuntimeException(
                        STR."expected END or DOT but has \{input.get(i + 2)}"
                    );
                };
                default -> throw new RuntimeException(
                    STR."expected END or DOT or TEXT but has \{input.get(i + 1)}"
                );
            };
            default -> throw new RuntimeException(
                STR."expected END or TEXT but has \{input.get(i)}"
            );
        };
    }

    // FIXME: rename ???
    public sealed interface FieldType {
        String javaClassName();
    }
    public record ScalarType(
        @Nullable Boolean typeIsNullable,
        String javaClassName
    ) implements FieldType { }
    public record AggregatedType(
        @Nullable String javaClassName, /* FIXME: remove???  */
        List<Field> fields
    ) implements FieldType { }
    public record Field(FieldType type, String name) { }
    record NumberedLine(int n, Line line) { }

    private static List<Field> buildGroup(
        List<NumberedLine> lines, Function<String, String> sqlTypeToJava
    ) {
        // FIXME: check that at least one local ???
        var locals = lines.stream()
            .filter(nl -> nl.line.chain.next == null).toList();

        var baseNumber = locals.getFirst().n;
        for (var i = 0; i < locals.size(); i++) {
            if (i + locals.get(i).n != baseNumber)
                throw new RuntimeException(
                    "The declarations of the members of the group should run consecutively"
                );
        }

        var localsChecked = locals.stream().map(nl -> {
            if (nl.line.chain.javaFieldName == null)
                throw new RuntimeException("Field name required");

            if (nl.line.chain.javaTypeName != null)
                throw new RuntimeException("Aggregated java class name not expected here");

            return new Field(
                new ScalarType(false, sqlTypeToJava.apply(nl.line.sqlTypeName)),
                nl.line.chain.javaFieldName
            );
        });

        var next = lines.stream()
            .filter(nl -> nl.line.chain.next != null)
            .collect(
                Collectors.groupingBy(
                    nl -> {
                        var javaFieldName = nl.line.chain.next.javaFieldName;
                        if (javaFieldName == null)
                            throw new RuntimeException("Field name required");

                        return javaFieldName;
                    },
                    LinkedHashMap::new, Collectors.toList()
                )
            ).entrySet().stream().map(group -> {
                var groupLines = group.getValue();
                var aggregatedTypeName =
                    groupLines.getFirst().line.sqlTypeName;

                if (aggregatedTypeName == null)
                    throw new RuntimeException("Aggregated java class name required");

                return new Field(
                    new AggregatedType(aggregatedTypeName, buildGroup(groupLines, sqlTypeToJava)),
                    group.getKey()
                );
            });

        return Stream.concat(localsChecked, next).toList();
    }

    public static List<Field> parseResultSetColumnNames(
        List<String> columnNames, Function<String, String> sqlTypeToJava
    ) {
        return buildGroup(
            IntStream
                .range(0, columnNames.size())
                .mapToObj(n -> new NumberedLine(n, parse(lex(columnNames.get(n)))))
                .toList(),
            sqlTypeToJava
        );
    }

//    ds."""
//                select
//                    c.id        as ":t user         id                       ",
//                    c.name      as "                name                     ",
//                    v.joined    as "                collection.              ",
//                    user.id     as "        Patient patients.     id         ",
//                    user.name   as "                patients.     name       ",
//                    bank.id     as "                patients.Bank banks.id   ",
//                    bank.money  as "                patients.     banks.money",
//                    doctor.id   as "        Doctor  doctors.id               ",
//                    doctor.name as "                doctors.name             "
//                from clinics c
//                inner join doctors d on d.clinic_id = c.id
//                inner join users   u on u.clinic_id = c.id
//                inner join banks   b on b.user_id   = u.id
//            """.g.fetchOne(Clinic.class);

//    ds."""
//                select
//                    c.id        as ":t user id                         ",
//                    c.name      as "        name                       ",
//                    v.joined    as "        collection.                ",
//                    user.id     as "        patients(Patient).id       ",
//                    user.name   as "        patients         .name     ",
//                    bank.id     as "        patients.banks(Bank).id    ",
//                    bank.money  as "        patients.banks       .money",
//                    doctor.id   as "        doctors(Doctor).id         ",
//                    doctor.name as "        doctors        .name       "
//                from clinics c
//                inner join doctors d on d.clinic_id = c.id
//                inner join users   u on u.clinic_id = c.id
//                inner join banks   b on b.user_id   = u.id
//            """.g.fetchOne(Clinic.class);
}
