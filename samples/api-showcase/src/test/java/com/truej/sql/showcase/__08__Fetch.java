package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __08__Fetch {
    @Test
    void fetch(DataSource ds) {
        assertEquals(
            Stmt."select name from users".fetch(ds, ctx -> "some")
                , "some"
        );
    }
}
