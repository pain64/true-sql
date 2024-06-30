package com.truej.sql.showcase;

import static com.truej.sql.v3.source.Parameters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class __07__Call {

    void simple(MainDataSource ds) {
        assertEquals(
            ds.q("{ call ? = some_procedure(?, ?) }", out(), 42, inout(42))
                .asCall().fetchOne(Integer.class)
            , 84
        );
    }
}
