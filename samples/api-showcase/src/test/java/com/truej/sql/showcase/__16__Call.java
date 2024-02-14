package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static com.truej.sql.v3.TrueJdbc.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __16__Call {
    @Test
    void fetch(DataSource ds) {
        // Stored procedure get parameter Name
        // Check metadata
        // Check with cursor
        // TODO: fetchOne only???
        assertEquals(
            Call."call \{out("result")} = some_procedure(\{42}, \{inout("a", 42)}})"
                .fetchOne(ds, String.class)
            , List.of("Ivan", "Joe")
        );
    }
}
