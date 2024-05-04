package com.truej.sql.showcase;

import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.fetch.*;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.truej.sql.v3.TrueSql.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {

    @Test void test(MainDataSource ds) {
        assertEquals(
            ds."insert into users(name, email) values('John', 'xxx@email.com')"
                .asGeneratedKeys("id")
                .fetchUpdateCount(new FetcherStream<>(Long.class))
            , 1L
        );
    }
}
