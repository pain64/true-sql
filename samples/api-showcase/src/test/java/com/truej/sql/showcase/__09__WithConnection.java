package com.truej.sql.showcase;

import com.truej.sql.v3.TrueSql;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueSql.Stmt;
import static com.truej.sql.v3.TrueSql.m;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __09__WithConnection {
    @Test void simple(MainDataSource ds) {
        assertEquals(
            ds.withConnection(cn -> {
                cn.inTransaction(() -> null);

                cn."""
                    create temp table temp_table as
                    with t (s) AS ( values ('a'), ('b') )
                    """.fetchNone();

                return cn."select * from temp_table"
                    .fetchList(String.class);
            })
            , List.of("a", "b")
        );
    }
}
