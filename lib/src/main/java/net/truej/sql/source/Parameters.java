package net.truej.sql.source;

import java.util.List;

import static net.truej.sql.dsl.MissConfigurationException.raise;

public class Parameters {
    public static final class AsNullable { }
    public static final class AsNotNull { }

    public static final AsNullable Nullable = new AsNullable();
    public static final AsNotNull NotNull = new AsNotNull();

    public interface ArgumentsExtractor<T> {
        Object[] extract(T one);
    }

    public static <T> Void unfold(List<T> many) { return raise(); }

    public static <T> Void unfold(List<T> many, ArgumentsExtractor<T> extractor) { return raise(); }

    public static Void out(Class<?> toClass) { return raise(); }

    public static <T> Void inout(T value) { return raise(); }
}
