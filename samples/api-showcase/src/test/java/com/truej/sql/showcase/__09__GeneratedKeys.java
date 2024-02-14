package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;

// TODO: `with` example
public class __09__GeneratedKeys {
    @Test
    void fetch(DataSource ds) {
        Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
            .withGeneratedKeys().fetchOne(ds, Long.class);
    }
}
