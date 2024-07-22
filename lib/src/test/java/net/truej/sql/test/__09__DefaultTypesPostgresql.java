package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __09__DefaultTypesPostgresql {

    record DataTypes(
        BigDecimal bigdecimal_type, @Nullable BigDecimal bigdecimal_type_null,
        Boolean boolean_type, @Nullable Boolean boolean_type_null,
        LocalDate date_type, @Nullable LocalDate date_type_null,
        Integer integer_type, @Nullable Integer integer_type_null,
        Long long_type, @Nullable Long long_type_null,
        String string_type, @Nullable String string_type_null,
        //Byte []  bytes_type, @Nullable Byte [] bytes_type_null,
        Integer short_type, @Nullable Integer short_type_null,
        LocalTime time_type, @Nullable LocalTime time_type_null,
        LocalDateTime timestamp_type, @Nullable LocalDateTime timestamp_type_null
    ) { }

    // FIXME: HSQLDB ???
    @TestTemplate @DisabledOn(HSQLDB) public void test(MainDataSource ds) {
        ds.q("""
                insert into all_default_data_types values(
                    ?, ?,
                    ?, ?,

                    ?, ?,
                    ?, ?,
                    ?, ?,
                    ?, ?,
                    ?, ?,
                    ?, ?,
                    ?, ?)
                """,
            new BigDecimal("100.24124"), null,
            Boolean.valueOf(true), null,
            //new byte[] {1}, null,
            LocalDate.of(2024, 7, 1), null,
            Integer.valueOf(100), null,
            Long.valueOf(99L), null,
            "hello", null,
            Short.valueOf("255"), null,
            LocalTime.of(23, 59, 59), null,
            LocalDateTime.of(1984, 1, 1, 23, 59, 59), null
        ).fetchNone();


        ds.q("""
            select
                bigdecimal_type, bigdecimal_type_null ,
                boolean_type, boolean_type_null,
                --bytearray_type bytea NOT NULL,
                --bytearray_type_null bytea,
                date_type, date_type_null,
                integer_type, integer_type_null,
                long_type, long_type_null,
                string_type, string_type_null,
                short_type, short_type_null,
                time_type, time_type_null,
                timestamp_type, timestamp_type_null
            from all_default_data_types
            """
        ).fetchOne(DataTypes.class);
    }
}
