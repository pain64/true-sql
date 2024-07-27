package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.compiler.TrueSqlTests2.DisabledOn;
import static net.truej.sql.source.Parameters.out;

// FIXME: mariadb
// TODO: test for postgresql
@ExtendWith(TrueSqlTests2.class) @EnableOn(MSSQL)
@TrueSql public class __17__TypeBindingsStoredProcedures_MSSQL {
    record DataTypes(
        @NotNull BigDecimal bigDecimalType, @Nullable BigDecimal bigDecimalTypeNull,
        @NotNull boolean booleanType, @Nullable Boolean booleanTypeNull,
        @NotNull LocalDate dateType, @Nullable LocalDate dateTypeNull,
        @NotNull int integerType, @Nullable Integer integerTypeNull,
        @NotNull long longType, @Nullable Long longTypeNull,
        @NotNull String stringType, @Nullable String stringTypeNull,
        @NotNull short shortType, @Nullable Short shortTypeNull,
        @NotNull byte byteType, @Nullable Byte byteTypeNull,
        @NotNull LocalTime timeType, @Nullable LocalTime timeTypeNull,
        @NotNull LocalDateTime timestampType, @Nullable LocalDateTime timestampTypeNull
    ) { }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
                {call test_types_procedure(
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?
                )}
                """,
            new BigDecimal("100.24124"), out(BigDecimal.class), out(BigDecimal.class),
            true, out(boolean.class), out(Boolean.class),
            //new byte[] {1}, null,
            LocalDate.of(2024, 7, 1), out(LocalDate.class), out(LocalDate.class),
            100, out(int.class), out(Integer.class),
            99L, out(long.class), out(Long.class),
            "hello", out(String.class), out(String.class),
            (short) 255, out(short.class), out(Short.class),
            (byte) 8, out(byte.class), out(Byte.class),
            LocalTime.of(23, 59, 59), out(LocalTime.class), out(LocalTime.class),
            LocalDateTime.of(1984, 1, 1, 23, 59, 59), out(LocalDateTime.class), out(LocalDateTime.class)
        ).asCall().fetchOne(DataTypes.class);
    }
}
