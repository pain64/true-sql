package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __10__DefaultTypesHSQLDB {
    record DataTypes(
        Integer byte_type, @Nullable Integer byte_null
    ) { }

    // FIXME: HSQLDB ???
    @TestTemplate
    @TrueSqlTests2.DisabledOn(POSTGRESQL) public void test(MainDataSource ds) {
        ds.q("""
                insert into all_default_data_types values(
                    ?, ?
                )
                """,
            Byte.valueOf("8"), null
        ).fetchNone();


        ds.q("""
            select
                byte_type, byte_type_null
            from all_default_data_types
            """
        ).fetchOne(DataTypes.class);
    }
}
