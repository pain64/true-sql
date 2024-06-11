package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.truej.sql.v3.source.Source.BatchSupplier.B;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Batch {
    static class BatchedParameters<T> {
        public T b;
        public BatchedParameters(List<T> parameters) {

        }
    }
    @Test void statement(MainDataSource ds) {
        var ids = new BatchedParameters<>(
            List.of("a", "b", "c")
        );
        // Batched.exec(
        //     List.of("a", "b", "c"),
        //     s -> ds."insert into t1 values(\{s})"
        //         .asGeneratedKeys("id").withUpdateCount.fetchOne(Long.class)
        // )

        assertEquals(
            ds.batched."""
                insert into t1 values(\{b(ids)})
                """.asGeneratedKeys("id").fetchList(Long.class)
            , List.of(1L, 2L, 3L)
        );

        // FIXME: fetch stream allowed only on ConnectionW ???
        // FIXME: drop fetchManual ???
        assertEquals(
            ds.batch(
                List.of("a", "b", "c"),
                s ->
                    B."insert into t1 values(\{s})"
            ).asGeneratedKeys("id").fetchList(Long.class)
            , List.of(1L, 2L, 3L)
        );
    }
}
