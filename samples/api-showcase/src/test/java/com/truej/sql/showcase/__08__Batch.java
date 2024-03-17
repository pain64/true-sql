package com.truej.sql.showcase;

import com.truej.sql.v3.TrueSql;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueSql.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Batch {
    @Test void statement(DataSource ds) {
        assertEquals(
            TrueSql.batchStmt(
                List.of("a", "b", "c"),
                s -> Stmt. "insert into t1 values(\{ s })"
            ).withGeneratedKeys().fetchList(ds, m(Long.class))
            , List.of(1L, 2L, 3L)
        );
    }

    @Test void call(DataSource ds) {
        TrueSql.batchCall(
            List.of(1, 2, 3),
            s -> Call. "call some_proc(\{ s })"
        ).fetchNone(ds);
    }
}
