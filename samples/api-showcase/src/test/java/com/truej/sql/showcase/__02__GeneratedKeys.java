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

        var name = "Joe";
        // Decimal:1231232398128937129381298378914289471298412894'
        // Stmt:(delete from users where name = {name}).fetchNone(ds);
        // trueSql.batch(
        //     (1, 2, 3), f Stmt(delete from users where id = {_})
        // ).fetchNone(ds)
        // trueSql.batch(
        //     (1, 2, 3), f Stmt〉
        //         delete from users
        //         where id = {_} and age > 18
        // ).fetchNone(ds)
        // Stmt("delete from users where name = :1", name).fetchNone(ds);
        // Stmt."delete from users where name = \{name}".fetchNone(ds);


        // Stmt("delete from users where name = ${name}").fetchNone(ds)
        // TrueSql.batch(
        //     idsToDelete, { Stmt("delete from users where id = ${it}") }
        // ).fetchNone(ds)

         var idsToDelete = List.of(1, 2, 3);
         TrueSql.batch(
             idsToDelete, id -> Stmt."delete from users where id = \{id}"
         ).fetchNone(ds);

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
