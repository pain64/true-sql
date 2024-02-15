package com.truej.sql.showcase;

import com.truej.sql.v3.TrueJdbc;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static com.truej.sql.v3.TrueJdbc.m;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __09__WithConnection {
    @Test void simple(DataSource ds) {
        assertEquals(
            TrueJdbc.withConnection(ds, cn -> {
                Stmt."""
                        create temp table temp_table as
                        with t (s) AS ( values ('a'), ('b') )
                    """.fetchNone(cn);

                return Stmt."select * from temp_table"
                    .fetchList(cn, m(String.class));
            })
            , List.of("a", "b")
        );
    }
}
