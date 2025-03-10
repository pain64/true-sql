package net.truej.sql.test;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNull;

public class __09__Types {

    public record TypeSample<T>(T v) {
        void assertEquals(T other) {
            if (v instanceof byte[])
                Assertions.assertArrayEquals((byte[]) v, (byte[]) other);
            else
                Assertions.assertEquals(v, other);
        }
        public void eq(Function<T, T> select, Function<T, T> call) {
            assertEquals(select.apply(v));
            assertNull(select.apply(null));

            assertEquals(call.apply(v));
            assertNull(call.apply(null));
        }
    }

    public static final TypeSample<Boolean> BOOLEAN = new TypeSample<>(true);
    public static final TypeSample<Byte> BYTE = new TypeSample<>((byte) 1);
    public static final TypeSample<Short> SHORT = new TypeSample<>((short) 1);
    public static final TypeSample<Integer> INTEGER = new TypeSample<>(1);
    public static final TypeSample<Long> LONG = new TypeSample<>(1L);
    public static final TypeSample<Float> FLOAT = new TypeSample<>(3.14159f);
    public static final TypeSample<Double> DOUBLE = new TypeSample<>(3.1415926535d);
    public static final TypeSample<BigDecimal> BIG_DECIMAL = new TypeSample<>(new BigDecimal(("100.241")));
    public static final TypeSample<String> STRING = new TypeSample<>("x");
    public static final TypeSample<LocalDate> LOCAL_DATE = new TypeSample<>(LocalDate.of(1970, 1, 1));
    public static final TypeSample<LocalTime> LOCAL_TIME = new TypeSample<>(LocalTime.of(23, 59, 59));
    public static final TypeSample<LocalDateTime> LOCAL_DATE_TIME =
        new TypeSample<>(LocalDateTime.of(1984, 1, 1, 23, 59, 59));
    public static final TypeSample<OffsetDateTime> OFFSET_DATE_TIME =
        new TypeSample<>(OffsetDateTime.of(1984, 1, 1, 23, 59, 59, 0, ZoneOffset.UTC));
    public static final TypeSample<OffsetTime> OFFSET_TIME =
        new TypeSample<>(OffsetTime.of(23, 59, 59, 0, ZoneOffset.ofHours(1)));
    public static final TypeSample<ZonedDateTime> ZONED_DATE_TIME =
        new TypeSample<>(ZonedDateTime.of(1984, 1, 1, 23, 59, 59, 0, ZoneOffset.UTC));
    public static final TypeSample<byte[]> BYTE_ARRAY = new TypeSample<>(new byte[]{1, 2, 3});
    public static final TypeSample<UUID> _UUID =
        new TypeSample<>(UUID.fromString("b08656ed-f7a8-11ef-a66a-50ebf6945ad6"));
}