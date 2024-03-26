package com.truej.sql.showcase;

import com.truej.sql.v3.fetch.*;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.util.function.Function;

import static com.truej.sql.v3.TrueSql.Stmt;
import static com.truej.sql.v3.TrueSql.m;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {

    @Test void test(MainDataSource ds) {
        var xx = Stmt."insert into users(name, email) values('John', 'xxx@email.com') returning id"
            .fetchUpdateCount(ds, new FetcherOne<>(m(Long.class)));

        assertEquals(
            Stmt."insert into users(name, email) values('John', 'xxx@email.com')"
                .withGeneratedKeys("id")
                .fetchUpdateCount(
                    ds, new FetcherGeneratedKeys<>(
                        new FetcherStream<>(m(Long.class))
                    )
                )
            , (Long) 1L
        );
    }
}
