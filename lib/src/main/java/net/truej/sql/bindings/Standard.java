package net.truej.sql.bindings;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Standard {
    public record Binding(
        String className, String rwClassName, boolean mayBeNullable,
        @Nullable Integer compatibleSqlType, @Nullable String compatibleSqlTypeName
    ) {}

    public static final List<Binding> bindings = List.of(
        new Binding("boolean", "PrimitiveBooleanReadWrite", false, null, null),
        new Binding("java.lang.Boolean", "BooleanReadWrite", true, null, null),
        new Binding("byte", "PrimitiveByteReadWrite", false, null, null),
        new Binding("java.lang.Byte", "ByteReadWrite", true, null, null),
        new Binding("short", "PrimitiveShortReadWrite", false, null, null),
        new Binding("java.lang.Short", "ShortReadWrite", true, null, null),
        new Binding("int", "PrimitiveIntReadWrite", false, null, null),
        new Binding("java.lang.Integer", "IntegerReadWrite", true, null, null),
        new Binding("long", "PrimitiveLongReadWrite", false, null, null),
        new Binding("java.lang.Long", "LongReadWrite", true, null, null),
        new Binding("java.lang.String", "StringReadWrite", true, null, null),
        new Binding("java.sql.Date", "DateReadWrite", true, null, null),
        new Binding("java.sql.Time", "TimeReadWrite", true, null, null),
        new Binding("java.sql.Timestamp", "TimestampReadWrite", true, null, null),
        new Binding("java.math.BigDecimal", "BigDecimalReadWrite", true, null, null),
        new Binding("byte[]", "ByteArrayReadWrite", true, null, null),
        new Binding("java.net.URL", "UrlReadWrite", true, null, null)
    );
}
