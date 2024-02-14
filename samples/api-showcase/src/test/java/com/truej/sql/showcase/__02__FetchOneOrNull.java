package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __02__FetchOneOrNull {
    @Test void fetch(DataSource ds) {
        assertNull(Stmt."select name from users where id = \{1}".fetchOneOrNull(ds, String.class));
    }
}
