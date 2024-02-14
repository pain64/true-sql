package com.truej.sql.showcase;

import com.truej.sql.v3.prepare.Batch;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Stmt;

public class __15__Batch {
    @Test void test(DataSource ds) {

        // FIXME: allow only fetchNone, fetchArray, fetchList, fetchStream

        var data = List.of("a", "b", "c");

        var insertedIds = Batch.stmt(
            data, s -> Stmt."insert into t1 values(\{s})"
        ).withGeneratedKeys().fetchList(ds, Long.class);

        Batch.call(
            data, s -> Call."call some_proc(\{s})"
        ).fetchNone(ds);

        // TODO: call also
    }
}
