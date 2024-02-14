package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;

public class __04__FetchNone {
    @Test void fetch(DataSource ds) {
        Stmt."insert into users values(1, 'John', 'xxx@email.com')".fetchNone(ds);
    }
}
