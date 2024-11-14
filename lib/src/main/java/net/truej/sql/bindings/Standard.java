package net.truej.sql.bindings;

import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.List;

public class Standard {
    public record Binding(
        String className, String rwClassName,
        @Nullable Integer compatibleSqlType, @Nullable String compatibleSqlTypeName
    ) {}

    public static final List<Binding> bindings = List.of(
        new Binding("boolean", BooleanReadWrite.class.getName(), null, null),
        new Binding("java.lang.Boolean", BooleanReadWrite.class.getName(), null, null),
        new Binding("byte", ByteReadWrite.class.getName(), null, null),
        new Binding("java.lang.Byte", ByteReadWrite.class.getName(), null, null),
        new Binding("short", ShortReadWrite.class.getName(), null, null),
        new Binding("java.lang.Short", ShortReadWrite.class.getName(), null, null),
        new Binding("int", IntegerReadWrite.class.getName(), null, null),
        new Binding("java.lang.Integer", IntegerReadWrite.class.getName(), null, null),
        new Binding("long", LongReadWrite.class.getName(), null, null),
        new Binding("java.lang.Long", LongReadWrite.class.getName(), null, null),
        new Binding("java.lang.String", StringReadWrite.class.getName(), null, null),

        new Binding("float", FloatReadWrite.class.getName(), null, null),
        new Binding("java.lang.Float", FloatReadWrite.class.getName(), null, null),
        new Binding("double", DoubleReadWrite.class.getName(), null, null),
        new Binding("java.lang.Double", DoubleReadWrite.class.getName(), null, null),

        new Binding("java.time.LocalDate", LocalDateReadWrite.class.getName(), Types.DATE, null),
        new Binding("java.time.LocalTime", LocalTimeReadWrite.class.getName(), Types.TIME, null),
        new Binding("java.time.LocalDateTime", LocalDateTimeReadWrite.class.getName(), Types.TIMESTAMP, null),

        new Binding("java.time.OffsetDateTime", OffsetDateTimeReadWrite.class.getName(), Types.TIMESTAMP_WITH_TIMEZONE, null),
        new Binding("java.time.OffsetTime", OffsetTimeReadWrite.class.getName(), Types.TIME_WITH_TIMEZONE, null),
        new Binding("java.time.ZonedDateTime", ZonedDateTimeReadWrite.class.getName(), Types.TIMESTAMP_WITH_TIMEZONE, null),

        new Binding("java.math.BigDecimal", BigDecimalReadWrite.class.getName(), null, null),
        new Binding("[B", ByteArrayReadWrite.class.getName(), null, null)
    );
}
