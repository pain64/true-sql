package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __03__UpdateCount {
    @Test void test(DataSource ds) {
        assertEquals(
            Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
                .withUpdateCount().fetchNone(ds).count
            , 1L
        );
    }
}
