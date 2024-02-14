package com.truej.sql.showcase;

import com.truej.sql.v3.TrueJdbc;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Call;
import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Batch {
    @Test void statement(DataSource ds) {
        assertEquals(
            TrueJdbc.batchStmt(
                List.of("a", "b", "c"),
                s -> Stmt. "insert into t1 values(\{ s })"
            ).withGeneratedKeys().fetchList(ds, Long.class)
            , List.of(1L, 2L, 3L)
        );
    }

    @Test void call(DataSource ds) {
        TrueJdbc.batchCall(
            List.of(1, 2, 3),
            s -> Call. "call some_proc(\{ s })"
        ).fetchNone(ds);
    }
}
