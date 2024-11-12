package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests2.Database.MSSQL;

@ExtendWith(TrueSqlTests2.class) @Disabled
@TrueSql public class __09__DefaultTypesMSSQL {
    record DataTypes(
        byte aByte, @Nullable Byte aByteNullable,
        byte[] bytes, @Nullable byte[] bytesNullable,
        OffsetDateTime offsetDateTime, @Nullable OffsetDateTime offsetDateTimeNullable
        //,
        //OffsetTime offsetTime, @Nullable OffsetTime offsetTimeNullable
    ) { }

    @TestTemplate
    public void test(MainDataSource ds) {
        ds.withConnection(cn -> {
            var data = new DataTypes(
                (byte) 1, null,
                new byte[] {(byte) 1}, null,
                OffsetDateTime.of(
                    2024, 12, 31,
                    23, 59, 59, 0, ZoneOffset.ofHours(0)
                ), null
                //,
                //OffsetTime.of(23,59,59,0, ZoneOffset.ofHours(5)), null

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

            Assertions.assertEquals(data, fetched);
            return null;
        });
    }
}