package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueSql.*;
import static com.truej.sql.v3.TrueSql.CallParameters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __07__Call {
    @Test
    void simple(MainDataSource ds) {
//        ds."{ call \{out("result")} = some_procedure(\{42}, \{inout("a", 42)}) }"
//            .asCall().fetchOne(String.class);

        // Stored procedure get parameter Name
        // Check metadata
        // Check with cursor
        assertEquals(
            ds."{ call \{out("result")} = some_procedure(\{42}, \{inout("a", 42)}) }"
                .asCall().fetchOne(String.class)
            , "xxx"
        );
    }
}
