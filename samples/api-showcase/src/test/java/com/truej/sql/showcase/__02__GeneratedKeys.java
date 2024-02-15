package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static com.truej.sql.v3.TrueJdbc.m;

public class __02__GeneratedKeys {
    @Test void test(DataSource ds) {
        Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
            .withGeneratedKeys().fetchOne(ds, m(Long.class));
    }
}
