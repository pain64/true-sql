package net.truej.sql.bindings;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Standard {
    public record Binding(
        String className, String rwClassName, boolean mayBeNullable,
        @Nullable Integer compatibleSqlType, @Nullable String compatibleSqlTypeName
    ) {}

    public static final List<Binding> bindings = List.of(
        new Binding("boolean", "net.truej.sql.bindings.PrimitiveBooleanReadWrite", false, null, null),
        new Binding("java.lang.Boolean", "net.truej.sql.bindings.BooleanReadWrite", true, null, null),
        new Binding("byte", "net.truej.sql.bindings.PrimitiveByteReadWrite", false, null, null),
        new Binding("java.lang.Byte", "net.truej.sql.bindings.ByteReadWrite", true, null, null),
        new Binding("short", "net.truej.sql.bindings.PrimitiveShortReadWrite", false, null, null),
        new Binding("java.lang.Short", "net.truej.sql.bindings.ShortReadWrite", true, null, null),
        new Binding("int", "net.truej.sql.bindings.PrimitiveIntReadWrite", false, null, null),
        new Binding("java.lang.Integer", "net.truej.sql.bindings.IntegerReadWrite", true, null, null),
        new Binding("long", "net.truej.sql.bindings.PrimitiveLongReadWrite", false, null, null),
        new Binding("java.lang.Long", "net.truej.sql.bindings.LongReadWrite", true, null, null),
        new Binding("java.lang.String", "net.truej.sql.bindings.StringReadWrite", true, null, null),
        new Binding("java.sql.Date", "net.truej.sql.bindings.DateReadWrite", true, null, null),
        new Binding("java.sql.Time", "net.truej.sql.bindings.TimeReadWrite", true, null, null),
        new Binding("java.sql.Timestamp", "net.truej.sql.bindings.TimestampReadWrite", true, null, null),
        new Binding("java.math.BigDecimal", "net.truej.sql.bindings.BigDecimalReadWrite", true, null, null),
        new Binding("byte[]", "net.truej.sql.bindings.ByteArrayReadWrite", true, null, null),
        new Binding("java.net.URL", "net.truej.sql.bindings.UrlReadWrite", true, null, null)
    );
}
