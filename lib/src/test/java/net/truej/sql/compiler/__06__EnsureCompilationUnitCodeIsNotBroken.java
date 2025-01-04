package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

public class __06__EnsureCompilationUnitCodeIsNotBroken {
    @Test public void test() throws SQLException {
        Assertions.assertNull(TypeFinder.ensureCompilationUnitCodeIsNotBroken(null));
    }
}
