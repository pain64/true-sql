package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import static com.truej.sql.v3.prepare.Parameters.*;
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
            // ds."{ \{out("result")} = call some_procedure(\{42}, \{inout("a", 42)}) }"
            // out, inout parameters - 100%
            // { call
            // call

            ds."{ call some_procedure(\{42}, \{inout("a", 42)}) }"
                .asCall().fetchOutParameters(String.class)
//                .fetch(new ManagedAction<CallableStatement, Void, String>() {
//                    @Override public boolean willStatementBeMoved() { return false; }
//                    @Override public String apply(
//                        RuntimeConfig conf, Void executionResult,
//                        CallableStatement stmt, boolean hasGeneratedKeys
//                    ) throws SQLException {
//                        return null;
//                    }
//                })
                //.fetchOutParameters(String.class)
            , "xxx"
        );
    }
}
