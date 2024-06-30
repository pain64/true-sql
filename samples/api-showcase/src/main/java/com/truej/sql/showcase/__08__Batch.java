package com.truej.sql.showcase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Batch {
    void statement(MainDataSource ds) {
        assertEquals(
            ds.q(
                List.of("a", "b", "c"),
                "insert into t1 values(.1)",
                s -> new Object[]{s}
            ).asGeneratedKeys("id").fetchList(Long.class)
            , List.of(1L, 2L, 3L)
        );
    }
}
