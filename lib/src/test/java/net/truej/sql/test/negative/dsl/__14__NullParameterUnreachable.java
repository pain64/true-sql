package net.truej.sql.test.negative.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import net.truej.sql.bindings.NullParameter;
import net.truej.sql.bindings.NullParameter.UnreachableException;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class __14__NullParameterUnreachable {
    @Test void test() throws SQLException {
        var np = new NullParameter();
        Assertions.assertThrows(UnreachableException.class,
            () -> np.get((ResultSet) null, 1)
        );
        Assertions.assertThrows(UnreachableException.class,
            () -> np.get((CallableStatement) null, 1)
        );
        Assertions.assertThrows(UnreachableException.class,
            () -> np.registerOutParameter(null, 1)
        );
    }
}
