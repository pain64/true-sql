package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueSql.Stmt;
import static com.truej.sql.v3.TrueSql.m;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {
    @Test void test(DataSource ds) {
        assertEquals(
            Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
                .withGeneratedKeys().fetchOne(ds, m(Long.class))
            , 1L
        );
    }
}
