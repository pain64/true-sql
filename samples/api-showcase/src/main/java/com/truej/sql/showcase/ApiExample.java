package com.truej.sql.showcase;

import com.truej.sql.test.Diff;
import com.truej.sql.test.TestApi;

import javax.sql.DataSource;
import java.util.Date;
import java.util.UUID;

import static com.truej.sql.test.Diff.*;

public class ApiExample {
    interface TimeProvider {
        Date now();
    }

    interface UuidGenerator {
        UUID next();
    }

    void assertWithDatabase(DataSource dataSource, Runnable action, Diff diff) {

    }

    void assertWithDatabase(DataSource dataSource, Runnable action) {
        new Diff();
    }

    void f(TimeProvider ioTime, UuidGenerator ioUuid) {

    }
    // TODO: Вынести в отдельный API

    void testF() {
        var now = new Date();
        var uuid = UUID.randomUUID();
        TestApi.assertWith(() -> {
            // test scenario
            // UUID
            f(() -> now, () -> uuid);
        }, new Diff(TrueSql.SQL."""
            +users(\{uuid}, \{now}, 'joe@gmail.com')
            -users(2, 'Ivan', 'ivan@ya.ru'   )"""
        ));
    }

    void testF2() {
        var now = new Date(1298321381); // concrete unix time
        var uuid = UUID.fromString("abcdeeff");   // concrete uuid
        TestApi.assertWith(() -> {
            // test scenario
            // UUID
            f(() -> now, () -> uuid);
        }, new Diff("""
            +users(abcdeeff, 1298321381, 'joe@gmail.com')
            -users(2, 'Ivan', 'ivan@ya.ru'   )"""
        ));
    }

    void testF3() {
        DataSource ds = null;
        assertWithDatabase(ds, () -> {
            // test scenario
            // concrete unix time and UUID
            f(() -> new Date(1298321381), () -> UUID.fromString("abcdeeff"));
        }, new Diff(
            // TODO: check that setString works for any column type in JDBC, otherwise do concat and escape '
            new Delete("users", "AAAA-FFFF", "2024-01-24 00:22:28.477117", "John", "xxx"),
            new Delete("users", "'AAAA-FFFF', '2024-01-24 00:22:28.477117'," +
                " 'John\\'t Doe', 'xxx'"
            ),
            // Custom column converter ???
            // if we see that database type is XXX -> "123" -> my_super_type_init(123)
            new Delete(
                "users", "'AAAA-FFFF', '2024-01-24 00:22:28.477117', 'John', 'xxx'"
            ),
            new Insert("users", "BBBB-FFFF", "2024-01-25 00:22:28.477117", "John"),
            new Insert("users", "BBBB-FFFF", "2024-01-25 00:22:28.477117", "John", "{1, 2, 3}")
        ));

        assertWithDatabase(ds, () -> {

        });
    }

    // -- global config
    // 1. start database server
    //    1.1 server already started
    //    1.2 need to start server
    // 2. create schema
    // 3. enhance schema with special tables
    //       users -> users_ini, users_add, users_del
    //       sql: create table as
    // -- each @Test
    // 4. fill test data (fixture)
    // 5. copy *table -> *table_ini
    // 6. run test case
    // 7. check database state (assertion)
    //    users vs users_ini
    //    Generic API: table -> dump, dump -> table
    //    select * from users except select * from users_ini
    //       pg_dump users_add, users_del
    //
    void test() {
        TestApi.assertWith(() -> {
            // test scenario
            // UUID
        }, new Diff("""
            -users(1, 'John', 'joe@gmail.com')
            +users(2, 'Ivan', 'ivan@ya.ru'   )"""
        ));

        TestApi.regressedWith(() -> {
            // test scenario
        });
    }
}
