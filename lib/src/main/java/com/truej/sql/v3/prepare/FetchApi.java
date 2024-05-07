package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FetchApi<P extends PreparedStatement, R, U> {
    // FIXME:
    default <T> T delegated() { throw new RuntimeException("delegated"); }

    default Void fetchNone() { return delegated(); }

    default <T> T fetchOne(Class<T> toClass) { return delegated(); }

    default <T> Optional<T> fetchOneOptional(Class<T> toClass) { return delegated(); }

    default <T> @Nullable T fetchOneOrNull(Class<T> toClass) { return delegated(); }

    default <T> List<T> fetchList(Class<T> elementClass) { return delegated(); }

    default <T> List<T> fetchList(Class<T> elementClass, int expectedSize) { return delegated(); }

    default <T> Stream<T> fetchStream(Class<T> elementClass) { return delegated(); }

    default <T, PP extends PreparedStatement> T fetch(ManagedAction<PP, R, T> next) { return delegated(); }

    default U fetchUpdateCount() { return delegated(); }

    default <T> UpdateResult<U, T> fetchUpdateCount(ManagedAction<P, R, T> next) { return delegated(); }

    default <T> T fetchOutParameters(Class<T> toClass) { return delegated(); }
}
