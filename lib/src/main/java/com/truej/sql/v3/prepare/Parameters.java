package com.truej.sql.v3.prepare;

import java.util.List;

public class Parameters {
    public record Pair<A, B>(A a, B b) {}
    public record Triple<A, B, C>(A a, B b, C c) {}
    public record Quad<A, B, C, D>(A a, B b, C c, D d) {}

    public static <T> Void unfold(List<T> many) { return null; }
    public static <A, B> Void unfold2(List<Pair<A, B>> many) { return null; }
    public static <A, B, C> Void unfold3(List<Triple<A, B, C>> many) { return null; }
    public static <A, B, C, D> Void unfold4(List<Quad<A, B, C, D>> many) { return null; }

    public static Void out() { return null; }

    public static <T> Void inout(T value) {
        return null;
    }
}
