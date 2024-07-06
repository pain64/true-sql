package net.truej.sql.showcase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class __09__WithConnection {
    void simple(MainDataSource ds) {
        assertEquals(
            ds.withConnection(cn -> {
                cn.inTransaction(() -> null);

                // ConnectionW cnn = cn;

                cn.q("""
                    create temp table temp_table as
                    with t (s) AS ( values ('a'), ('b') )
                    """).fetchNone();

                return cn.q("select * from temp_table")
                    .fetchList(String.class);
            })
            , List.of("a", "b")
        );
    }
}
