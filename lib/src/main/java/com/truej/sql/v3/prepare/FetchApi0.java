package com.truej.sql.v3.prepare;

import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class FetchApi0<P extends PreparedStatement, R, U> {
    <T> T delegated() { throw new RuntimeException("delegated"); }

    public <T> T fetchOne(Class<T> toClass) { return delegated(); }

    public <T> Optional<T> fetchOneOptional(Class<T> toClass) { return delegated(); }

    public <T> @Nullable T fetchOneOrNull(Class<T> toClass) { return delegated(); }

    public <T> List<T> fetchList(Class<T> elementClass) { return delegated(); }

    public <T> List<T> fetchList(Class<T> elementClass, int expectedSize) { return delegated(); }

    public <T> Stream<T> fetchStream(Class<T> elementClass) { return delegated(); }

    public <T> T fetchOutParameters(Class<T> toClass) { return delegated(); }
}
