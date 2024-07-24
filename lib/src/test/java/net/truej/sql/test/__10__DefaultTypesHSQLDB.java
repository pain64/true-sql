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

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __10__DefaultTypesHSQLDB {
    record DataTypes(
        byte byte_type, @Nullable Byte byte_null
    ) { }

    // FIXME: HSQLDB ???
    @TestTemplate @DisabledOn(POSTGRESQL) public void test(MainDataSource ds) {
        ds.q("""
                insert into all_default_data_types values(
                    ?, ?
                )
                """,
            (byte) 8, (Byte) null
        ).fetchNone();


        ds.q("""
            select
                byte_type, byte_type_null
            from all_default_data_types
            """
        ).fetchOne(DataTypes.class);
    }
}
