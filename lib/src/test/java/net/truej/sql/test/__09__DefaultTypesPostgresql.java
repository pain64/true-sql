package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@Disabled
@ExtendWith(TrueSqlTests2.class) @EnableOn({POSTGRESQL, MYSQL})
@TrueSql public class __09__DefaultTypesPostgresql {

    record DataTypes(
        BigDecimal bigdecimal_type, @Nullable BigDecimal bigdecimal_type_null,
        boolean boolean_type, @Nullable Boolean boolean_type_null,
        LocalDate date_type, @Nullable LocalDate date_type_null,
        int integer_type, @Nullable Integer integer_type_null,
        long long_type, @Nullable Long long_type_null,
        String string_type, @Nullable String string_type_null,
        //Byte []  bytes_type, @Nullable Byte [] bytes_type_null,
        short short_type, @Nullable Short short_type_null,
        LocalTime time_type, @Nullable LocalTime time_type_null,
        LocalDateTime timestamp_type, @Nullable LocalDateTime timestamp_type_null
    ) { }

    @TestTemplate public void test(MainDataSource ds) {
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
            true, (Boolean) null,
            //new byte[] {1}, null,
            LocalDate.of(2024, 7, 1), null,
            100, (Integer) null,
            99L, (Long) null,
            "hello", null,
            (short) 255, (Short) null,
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
