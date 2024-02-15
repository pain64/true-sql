package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.*;
import static com.truej.sql.v3.TrueJdbc.CallParameters.inout;
import static com.truej.sql.v3.TrueJdbc.CallParameters.out;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __07__Call {
    @Test
    void simple(DataSource ds) {
        // Stored procedure get parameter Name
        // Check metadata
        // Check with cursor
        assertEquals(
            Call."call \{out("result")} = some_procedure(\{42}, \{inout("a", 42)}})"
                .fetchOne(ds, m(String.class))
            , "xxx"
        );
    }
}
