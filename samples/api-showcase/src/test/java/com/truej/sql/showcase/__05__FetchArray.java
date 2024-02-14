package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.List;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class __05__FetchArray {
    @Test void fetch(DataSource ds) {
        assertArrayEquals(
            Stmt."select name from users".fetchArray(ds, String.class)
            , List.of("Ivan", "Joe").toArray()
        );
    }
}
