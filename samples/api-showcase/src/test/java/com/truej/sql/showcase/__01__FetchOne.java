package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __01__FetchOne {
    @Test void fetch(DataSource ds) {
        assertEquals(
            Stmt."select name from users where id = \{42}".fetchOne(ds, String.class)
            , "Joe"
        );

//        withConnection(c -> {
//            c.prepareStatement()
//        })
//
//        var stmt = c.prepareStatement(SQL."""
//           select * from users where id = \{userId}
//        """).executeLargeUpdate().<List<@Nullable String>>execute();
//
//        var cstmt = c.prepareStatement("").getParameterMetaData();
//        cstmt.execute();
//        // 1. prepare{Statement, Call}
//        // 2. execute{Query, Update, Batch, Call}
//        MainDb db = null;
//        // db.queryOne()
    }
}
