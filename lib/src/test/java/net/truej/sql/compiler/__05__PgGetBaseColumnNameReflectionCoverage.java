package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

public class __05__PgGetBaseColumnNameReflectionCoverage {
    @Test public void test() throws SQLException {
        var cn = DriverManager.getConnection("jdbc:hsqldb:mem:db", "SA", null);
        var stmt = cn.prepareStatement("values 1");

        Assertions.assertThrows(RuntimeException.class, () ->
            JdbcMetadataFetcher.pgGetBaseColumnName(stmt.getMetaData(), 1)
        );
    }
}
