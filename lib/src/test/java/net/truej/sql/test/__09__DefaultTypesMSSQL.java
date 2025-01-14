package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests.Database.MSSQL;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(MSSQL)
@TrueSql public class __09__DefaultTypesMSSQL {
    record DataTypes(
        byte aByte, @Nullable Byte aByteNullable,
        byte[] bytes, @Nullable byte[] bytesNullable,
        OffsetDateTime offsetDateTime, @Nullable OffsetDateTime offsetDateTimeNullable
    ) { }

    record DataTypesSP(
        @NotNull byte[] bytes, @Nullable byte[] bytesNullable,
        @NotNull OffsetDateTime offsetDateTime, @Nullable OffsetDateTime offsetDateTimeNullable
    ) { }

    @TestTemplate
    public void testTables(MainDataSource ds) {
        ds.withConnection(cn -> {
            var data = new DataTypes(
                (byte) 1, null,
                new byte[] {(byte) 1}, null,
                OffsetDateTime.of(
                    2024, 12, 31,
                    23, 59, 59, 0, ZoneOffset.ofHours(0)
                ), null
            );
            cn.q("""
                    insert into all_default_data_types values(
                        ?, ?,
                        ?, ?,
                        ?, ?)
                    """,
                data.aByte, data.aByteNullable,
                data.bytes, data.bytesNullable,
                data.offsetDateTime, data.offsetDateTimeNullable
            ).fetchNone();

            var fetched = cn.q("""
                select
                    byte_type,
                    byte_type_null,
                    bytearray_type,
                    bytearray_type_null,
                    datetime_offset,
                    datetime_offset_null
                from all_default_data_types
                """
            ).fetchOne(DataTypes.class);
            Assertions.assertEquals(data.aByte, fetched.aByte);
            Assertions.assertEquals(data.aByteNullable, fetched.aByteNullable);
            Assertions.assertArrayEquals(data.bytes, fetched.bytes);
            Assertions.assertArrayEquals(data.bytesNullable, fetched.bytesNullable);
            Assertions.assertEquals(data.offsetDateTime, fetched.offsetDateTime);
            Assertions.assertEquals(data.offsetDateTimeNullable, fetched.offsetDateTimeNullable);

            return null;
        });
    }

    @TestTemplate public void testSP(MainDataSource ds) {
        ds.withConnection(cn -> {
            var data = new DataTypesSP(
                new byte[] {(byte) 1}, null,
                OffsetDateTime.of(
                    2024, 12, 31,
                    23, 59, 59, 0, ZoneOffset.ofHours(0)
                ), null
            );
            var fetched = cn.q("""
                    {call test_types_procedure_extended (
                        ?, ?,
                        ?, ?,
                        ?, ?)}
                    """,
                data.bytes, out(byte[].class), out(byte[].class),
                data.offsetDateTime, out(OffsetDateTime.class), out(OffsetDateTime.class)
            ).asCall().fetchOne(DataTypesSP.class);

            Assertions.assertArrayEquals(data.bytes, fetched.bytes);
            Assertions.assertArrayEquals(data.bytesNullable, fetched.bytesNullable);
            Assertions.assertEquals(data.offsetDateTime, fetched.offsetDateTime);
            Assertions.assertEquals(data.offsetDateTimeNullable, fetched.offsetDateTimeNullable);

            return null;
        });
    }
}
