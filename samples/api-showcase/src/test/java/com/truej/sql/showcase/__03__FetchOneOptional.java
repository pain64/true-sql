package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.Optional;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __03__FetchOneOptional {
    @Test void fetch(DataSource ds) {
        assertEquals(
            Stmt."select name from users where id = \{1}".fetchOneOptional(ds, String.class)
            , Optional.empty()
        );
    }
}
