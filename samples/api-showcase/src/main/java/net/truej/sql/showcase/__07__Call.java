package net.truej.sql.showcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static net.truej.sql.source.Parameters.*;

public class __07__Call {

    void simple(MainDataSource ds) {
        assertEquals(
            ds.q("{ call ? = some_procedure(?, ?) }", out(Integer.class), 42, inout(42))
                .asCall().fetchOne(Integer.class)
            , 84
        );
    }
}
