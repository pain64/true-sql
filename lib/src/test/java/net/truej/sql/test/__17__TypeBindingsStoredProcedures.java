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
import static net.truej.sql.source.Parameters.inout;
import static net.truej.sql.source.Parameters.out;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __17__TypeBindingsStoredProcedures {
    record DataTypes(
        BigDecimal bigdecimal_type, @Nullable BigDecimal bigdecimal_type_null,
        boolean boolean_type, @Nullable Boolean boolean_type_null,
        LocalDate date_type, @Nullable LocalDate date_type_null,
        int integer_type, @Nullable Integer integer_type_null,
        long long_type, @Nullable Long long_type_null,
        String string_type, @Nullable String string_type_null,
        short short_type, @Nullable Short short_type_null,
        byte byte_type, @Nullable Byte byte_type_null,
        LocalTime time_type, @Nullable LocalTime time_type_null,
        LocalDateTime timestamp_type, @Nullable LocalDateTime timestamp_type_null
    ) { }

    @TestTemplate @DisabledOn(POSTGRESQL) public void test(MainDataSource ds) {
        ds.q("""
                {call test_types_procedure(
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?)}
                """,
            inout(new BigDecimal("100.24124")), out(BigDecimal.class),
            inout(true), out(Boolean.class),
            //new byte[] {1}, null,
            inout(LocalDate.of(2024, 7, 1)), out(LocalDate.class),
            inout(100), out(Integer.class),
            inout(99L), out(Long.class),
            inout("hello"), out(String.class),
            inout((short) 255), out(Short.class),
            inout((byte) 8), out(Byte.class),
            inout(LocalTime.of(23, 59, 59)), out(LocalTime.class),
            inout(LocalDateTime.of(1984, 1, 1, 23, 59, 59)), out(LocalDateTime.class)
        ).asCall().fetchOne(DataTypes.class);

    }
}
