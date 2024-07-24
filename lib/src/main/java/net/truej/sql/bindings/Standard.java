package net.truej.sql.bindings;

import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.List;

public class Standard {
    public record Binding(
        String className, String rwClassName, boolean mayBeNullable,
        @Nullable Integer compatibleSqlType, @Nullable String compatibleSqlTypeName
    ) {}

    public static final List<Binding> bindings = List.of(
        new Binding("boolean", BooleanReadWrite.class.getName(), false, null, null),
        new Binding("java.lang.Boolean", BooleanReadWrite.class.getName(), true, null, null),
        new Binding("byte", ByteReadWrite.class.getName(), false, null, null),
        new Binding("java.lang.Byte", ByteReadWrite.class.getName(), true, null, null),
        new Binding("short", ShortReadWrite.class.getName(), false, null, null),
        new Binding("java.lang.Short", ShortReadWrite.class.getName(), true, null, null),
        new Binding("int", IntegerReadWrite.class.getName(), false, null, null),
        new Binding("java.lang.Integer", IntegerReadWrite.class.getName(), true, null, null),
        new Binding("long", LongReadWrite.class.getName(), false, null, null),
        new Binding("java.lang.Long", LongReadWrite.class.getName(), true, null, null),
        new Binding("java.lang.String", StringReadWrite.class.getName(), true, null, null),

        new Binding("java.time.LocalDate", LocalDateReadWrite.class.getName(), true, Types.DATE, null),
        new Binding("java.time.LocalTime", LocalTimeReadWrite.class.getName(), true, Types.TIME, null),
        new Binding("java.time.LocalDateTime", LocalDateTimeReadWrite.class.getName(), true, Types.TIMESTAMP, null),

        new Binding("java.time.OffsetDateTime", OffsetDateTimeReadWrite.class.getName(), true, Types.TIMESTAMP_WITH_TIMEZONE, null),
        new Binding("java.time.OffsetTime", OffsetTimeReadWrite.class.getName(), true, Types.TIME_WITH_TIMEZONE, null),
        new Binding("java.time.ZonedDateTime", ZonedDateTimeReadWrite.class.getName(), true, Types.TIMESTAMP_WITH_TIMEZONE, null),

        new Binding("java.math.BigDecimal", BigDecimalReadWrite.class.getName(), true, null, null),
        new Binding("byte[]", ByteArrayReadWrite.class.getName(), true, null, null)
    );
}
