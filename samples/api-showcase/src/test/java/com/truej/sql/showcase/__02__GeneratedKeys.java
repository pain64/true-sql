package com.truej.sql.showcase;

import com.truej.sql.v3.fetch.*;
import org.junit.jupiter.api.Test;

import static com.truej.sql.showcase.__02__GeneratedKeys.Transform.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {
    interface A<T> {}
    public enum Transform implements A {

    }

    @Test void test(MainDataSource ds) {
        // T -> T, T -> UpdateCount<U, T>
        // FetcherStream.fetch(
        //    base, mapper, transform, extra parameters
        // )
        assertEquals(
            ds."insert into users(name, email) values('John', 'xxx@email.com')"
                .asGeneratedKeys("id")
                //.g.withUpdateCount().g.fetchStream(Long.class)
                .withUpdateCount.g.fetchStream(Long.class)
                //.withUpdateCount.g.fetchStream(Long.class)
                //.updateCount.g.fetchStream(Long.class)
                //.fetchUpdateCount(new FetcherStream<>(Long.class))
            , 1L
        );
    }
}
