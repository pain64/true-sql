package net.truej.sql.source;

import java.util.List;

import static net.truej.sql.dsl.MissConfigurationException.raise;

public class Parameters {
    public static final class AsNullable { }
    public static final class AsNotNull { }

    public static final AsNullable Nullable = new AsNullable();
    public static final AsNotNull NotNull = new AsNotNull();

    public interface BatchArgumentsExtractor<T> {
        Object[] extract(T one);
    }

    public record Pair<A1, A2>(A1 a1, A2 a2) {}
    public record Triple<A1, A2, A3>(A1 a1, A2 a2, A3 a3) {}
    public record Quad<A1, A2, A3, A4>(A1 a1, A2 a2, A3 a3, A4 a4) {}

    //  FIXME: throw miss configuration
    public static <T> Void unfold(List<T> many) { return raise(); }
    public static <A, B> Void unfold2(List<Pair<A, B>> many) { return raise(); }
    public static <A, B, C> Void unfold3(List<Triple<A, B, C>> many) { return raise(); }
    public static <A, B, C, D> Void unfold4(List<Quad<A, B, C, D>> many) { return raise(); }

    public static Void out(Class<?> toClass) { return raise(); }

    public static <T> Void inout(T value) { return raise(); }
}
