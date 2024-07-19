package net.truej.sql.compiler;

import net.truej.sql.ConstraintViolationException;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.DataSourceW;
import net.truej.sql.test.DataBindings;
import org.hsqldb.HsqlException;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;

@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:hsqldb:mem:db",
        username = "SA",
        password = ""
//        url = "jdbc:postgresql://localhost:5432/uikit_sample",
//        username = "uikit",
//        password = "1234"
//    ),
//    typeBindings = {
//        @TypeBinding(
//            compatibleSqlType = Types.OTHER,
//            compatibleSqlTypeName = "point",
//            rw = DataBindings.PgPointRW.class
//        )
//    }
    )
    )
public record MainDataSource(DataSource w) implements DataSourceW {
    @Override
    public RuntimeException mapException(SQLException ex) {
        var x = 1;
        if (ex instanceof SQLIntegrityConstraintViolationException &&
            ex.getCause() instanceof HsqlException hex) {
            var parts = hex.getMessage().split(";");
            var constraintAndTable = parts[parts.length - 1].split("table:");

            return new ConstraintViolationException(
                constraintAndTable[1].trim(),
                constraintAndTable[0].trim()
            );
        }

        var pgUniqueConstraintCode = "23505";
        if (pgUniqueConstraintCode.equals(ex.getSQLState()) &&
            ex instanceof PSQLException pex) {
            return new ConstraintViolationException(
                pex.getServerErrorMessage().getTable(),
                pex.getServerErrorMessage().getConstraint()
            );
        }

        return DataSourceW.super.mapException(ex);
    }
}

