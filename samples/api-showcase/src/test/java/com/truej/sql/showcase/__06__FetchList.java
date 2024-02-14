package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __06__FetchList {
    @Test void fetch(DataSource ds) {
        assertEquals(
            Stmt."select name from users".fetchList(ds, String.class)
            , List.of("Ivan", "Joe")
        );
    }
}
