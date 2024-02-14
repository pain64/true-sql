package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __07__FetchStream {
    @Test void fetch(DataSource ds) {
        assertEquals(
            Stmt."select name from users".fetchStream(ds, String.class)
                .toList(), List.of("Ivan", "Joe")
        );
    }
}
