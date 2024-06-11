package com.truej.sql.v3.compiler;

import static com.truej.sql.v3.compiler.GLangParser.*;

public class StatementGenerator {
    sealed interface ParameterMode { }
    record BatchedParameters() implements ParameterMode { }
    record SingleParameters() implements ParameterMode { }

    sealed interface StatementMode { }
    record AsDefault() implements StatementMode { }
    record AsCall() implements StatementMode { }
    // FIXME
    record AsGeneratedKeysIndices() implements StatementMode { }
    record AsGeneratedKeysColumnNames() implements StatementMode { }

    sealed interface FetchMode { }
    sealed interface FetchTo extends FetchMode {
        FieldType toType();
    }

    record FetchNone() implements FetchMode { }
    record FetchOne(FieldType toType) implements FetchTo { }
    record FetchOneOrZero(FieldType toType) implements FetchTo { }
    record FetchList(FieldType toType) implements FetchTo { }
    record FetchStream(FieldType toType) implements FetchTo { }


    // Batched mode example
    // -- generated DTO's
    // <T, P1, P2, E extends Exception>
    // Stream<@Nullable Long> fetchStream_1(
    //     List<T> batch,
    //     ParamExtractor<T, P1, E> pe1,
    //     ParamExtractor<T, P2, E> pe2,
    //     ParamSetter<P1> ps1,
    //     ParamSetter<P2> ps2,
    //     Source source
    // ) {
    //     source.withConnection( connection -> ...
    //         var queryText = "...";
    //         var stmt = connection.prepareStatement(...); // managed with try-with-resource or ...
    //
    //         -- set parameters
    //         for (var element : batch) {
    //             var p1 = pe1.get(element);
    //             var p2 = pe2.get(element);
    //             ps1.set(1, p1);
    //             ps2.set(2, p2);
    //         }
    //
    //         -- execute simple
    //         stmt.execute();
    //         -- execute batched
    //         var updateCount = stmt.executeBatch();
    //
    //         -- do mapping to dest type (ScalarType or AggregatedType)
    //         -- with nullability checks
    //         -- Stream<T> as output
    //
    //         -- transform by Fetcher: one, oneOrZero, list, stream
    //
    //        -- wrap with UpdateCount if needed
    // }
    // Single mode example (with unfold)
    // <T, P1, P2x1, P2x2, E extends Exception>
    // Long fetchOne_2(
    //     P1 p1,
    //     ParamSetter<P1> ps1,
    //     List<Tuple2<P2x1, P2x2>> p2Unfold,
    //     ParamSetter<P2x1> ps2x1,
    //     ParamSetter<P2x2> ps2x2,
    //     Source source
    // ) {
    //     ps1.set(++n, p1);
    //     for (var element : p2Unfold) {
    //         ps2x1.set(++n, element.a1);
    //         ps2x2.set(++n, element.a2);
    //     }
    // }


    String generate(
        ParameterMode parameters,
        StatementMode prepareAs,
        FetchMode fetchAs,
        boolean withUpdateCount
    ) {
        var out = new StringBuilder();


        return out.toString();
    }
}
