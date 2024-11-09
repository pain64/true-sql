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

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.fetch.Parameters.inout;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests2.class) @EnableOn(ORACLE)
@TrueSql public class __17__TypeBindingsStoredProcedures_ORACLE {
    record DataTypes(
        @NotNull BigDecimal bigDecimalType, @Nullable BigDecimal bigDecimalTypeNull,
        @NotNull boolean booleanType, @Nullable Boolean booleanTypeNull,
        @NotNull LocalDate dateType, @Nullable LocalDate dateTypeNull,
        @NotNull int integerType, @Nullable Integer integerTypeNull,
        @NotNull long longType, @Nullable Long longTypeNull,
        @NotNull String stringType, @Nullable String stringTypeNull,
        @NotNull short shortType, @Nullable Short shortTypeNull,
        @NotNull byte byteType, @Nullable Byte byteTypeNull,
        @NotNull LocalDateTime timestampType, @Nullable LocalDateTime timestampTypeNull,
        @NotNull float floatType, @Nullable Float floatTypeNull,
        @NotNull double doubleType, @Nullable Double doubleTypeNull
    ) { }

    @TestTemplate public void test(MainDataSource ds) {
        // FIXME: add assert
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
                    ?,?,
                    ?,?
                )}
                """,
            inout(new BigDecimal("100.24124")), out(BigDecimal.class),
            inout(true), out(Boolean.class),
//            //new byte[] {1}, null,
            inout(LocalDate.of(2024, 7, 1)), out(LocalDate.class),
            inout(100), out(Integer.class),
            inout(99L), out(Long.class),
            inout("hello"), out(String.class),
            inout((short) 255), out(Short.class),
            inout((byte) 8), out(Byte.class),
            inout(LocalDateTime.of(1984, 1, 1, 23, 59, 59)), out(LocalDateTime.class),
            inout(3.14159f), out(Float.class),
            inout(3.1415926535d), out(Double.class)
        ).asCall().fetchOne(DataTypes.class);

    }
}
