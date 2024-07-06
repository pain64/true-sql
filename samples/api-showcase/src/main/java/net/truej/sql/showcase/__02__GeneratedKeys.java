package net.truej.sql.showcase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class __02__GeneratedKeys {
    void test(MainDataSource ds) {
        assertEquals(
            ds.q("insert into users(name, email) values('John', 'xxx@email.com')")
                .asGeneratedKeys("id").fetchOne(Long.class)
            , 1L
        );
    }
}
