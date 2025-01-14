package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.bindings.NullParameter;
import net.truej.sql.bindings.NullParameter.UnreachableException;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __14__NullParameterUnreacheble {
    @TestTemplate void test() throws SQLException {
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
