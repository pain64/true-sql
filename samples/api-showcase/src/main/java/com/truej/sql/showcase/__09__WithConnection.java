package com.truej.sql.showcase;

import com.truej.sql.v3.source.ConnectionW;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class __09__WithConnection {
    void simple(MainDataSource ds) {
        assertEquals(
            ds.withConnection((ConnectionW cn) -> {
                cn.inTransaction(() -> null);

                // ConnectionW cnn = cn;

                cn."""
                    create temp table temp_table as
                    with t (s) AS ( values ('a'), ('b') )
                    """.fetchNone();

                return cn."select * from temp_table"
                    .fetchList(String.class);
            })
            , List.of("a", "b")
        );
    }
}
