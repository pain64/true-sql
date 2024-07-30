package net.truej.sql.compiler;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import net.truej.sql.ConstraintViolationException;
import net.truej.sql.bindings.AsObjectReadWrite;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.DataSourceW;
import oracle.jdbc.OracleDatabaseException;
import org.hsqldb.HsqlException;
import org.postgresql.geometric.PGpoint;
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
    ),
    typeBindings = {
        @TypeBinding(
            compatibleSqlType = Types.OTHER,
            compatibleSqlTypeName = "point",
            rw = PgPointRW.class
        ),
        @TypeBinding(
            compatibleSqlType = Types.VARCHAR,
            compatibleSqlTypeName = "enum_user_sex",
            rw = PgUserSexRW.class
        ),
        @TypeBinding(
            compatibleSqlType = Types.OTHER,
            compatibleSqlTypeName = "point_nullable",
            rw = PgPointNotNullableRW.class
        )
    }
) public class MainDataSource extends DataSourceW {

    public MainDataSource(DataSource w) { super(w); }

    @Override public RuntimeException mapException(SQLException ex) {
        if (ex instanceof SQLIntegrityConstraintViolationException &&
            ex.getCause() instanceof HsqlException hex) {
            var parts = hex.getMessage().split(";");
            var constraintAndTable = parts[parts.length - 1].split("table:");

            return new ConstraintViolationException(
                null, null,
                constraintAndTable[1].trim(),
                constraintAndTable[0].trim()
            );
        }

        if (
            ex instanceof PSQLException pex &&
            ex.getSQLState().startsWith("23")
        ) {
            return new ConstraintViolationException(
                null,
                pex.getServerErrorMessage().getSchema(),
                pex.getServerErrorMessage().getTable(),
                pex.getServerErrorMessage().getConstraint()
            );
        }

        var mssqlUniqueConstraintCode = "23000";
        if (
            ex instanceof SQLServerException sex &&
            sex.getSQLState().equals(mssqlUniqueConstraintCode)
        ) {
            var message = sex.getMessage();
            var databaseName = message.replaceAll(".*database \"(\\S+)\".*", "$1");
            var tableAndSchema = message.replaceAll(".*table \"(\\S+)\".*", "$1").split("\\.");
            var constraintName = message.replaceAll(".*constraint \"(\\S+)\".*", "$1");

            return new ConstraintViolationException(
                databaseName, tableAndSchema[0], tableAndSchema[1], constraintName
            );
        }

        if (
            ex.getCause() != null && ex.getCause() instanceof OracleDatabaseException oex &&
            ex.getSQLState().startsWith("23")
        ) {
            var schemaAndConstraint = oex.getMessage()
                .replaceAll(".*\\((\\S+\\.\\S+)\\).*", "$1")
                .replace("\n", "").split("\\.");

            return new ConstraintViolationException(
                null, schemaAndConstraint[0], null, schemaAndConstraint[1]
            );
        }

        var mysqlConstraintCode = "23000";
        if (mysqlConstraintCode.equals(ex.getSQLState()) &&
            ex instanceof SQLIntegrityConstraintViolationException mySqlEx
        ) {
            var splitted = mySqlEx.getMessage().split("'");
            var tableAndConstraint = splitted[splitted.length - 1].split("\\.");

            return new ConstraintViolationException(
                null, null,
                tableAndConstraint[0],
                tableAndConstraint[1]
            );
        }

        return super.mapException(ex);
    }
}

