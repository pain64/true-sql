package com.truej.sql.showcase;

import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.prepare.Statement;
import com.truej.sql.v3.source.Source;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueSql.*;
import static com.truej.sql.v3.source.Source.BatchSupplier.B;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Batch {

    @Test void statement(MainDataSource ds) {
        assertEquals(
            ds.batch(
                List.of("a", "b", "c"),
                s -> B."insert into t1 values(\{s})"
            ).asGeneratedKeys("id").fetchList(Long.class)
            , List.of(1L, 2L, 3L)
        );
    }
}
