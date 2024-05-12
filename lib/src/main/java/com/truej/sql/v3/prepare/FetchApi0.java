package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class FetchApi0<P extends PreparedStatement, R, U> {
    private <T> T delegated() { throw new RuntimeException("delegated"); }

    public Void fetchNone() { return delegated(); }

    public <T> T fetchOne(Class<T> toClass) { return delegated(); }

    public <T> Optional<T> fetchOneOptional(Class<T> toClass) { return delegated(); }

    public <T> @Nullable T fetchOneOrNull(Class<T> toClass) { return delegated(); }

    public <T> List<T> fetchList(Class<T> elementClass) { return delegated(); }

    public <T> List<T> fetchList(Class<T> elementClass, int expectedSize) { return delegated(); }

    public <T> Stream<T> fetchStream(Class<T> elementClass) { return delegated(); }

    public <T, PP extends PreparedStatement> T fetch(ManagedAction<PP, R, T> next) { return delegated(); }

    public U fetchUpdateCount() { return delegated(); }

    public <T> UpdateResult<U, T> fetchUpdateCount(ManagedAction<P, R, T> next) { return delegated(); }

    public <T> T fetchOutParameters(Class<T> toClass) { return delegated(); }
}
