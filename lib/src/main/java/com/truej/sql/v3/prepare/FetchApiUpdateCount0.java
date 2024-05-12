package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.UpdateResult;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FetchApiUpdateCount0 <P extends PreparedStatement, R, U> {
    private <T> UpdateResult<U, T> delegated() { throw new RuntimeException("delegated"); }

    public U fetchNone() { return delegated().updateCount; }

    public <T> UpdateResult<U, T> fetchOne(Class<T> toClass) { return delegated(); }

    public <T> UpdateResult<U, Optional<T>> fetchOneOptional(Class<T> toClass) { return delegated(); }

    public <T> UpdateResult<U, @Nullable T> fetchOneOrNull(Class<T> toClass) { return delegated(); }

    public <T> UpdateResult<U, List<T>> fetchList(Class<T> elementClass) { return delegated(); }

    public <T> UpdateResult<U, List<T>> fetchList(Class<T> elementClass, int expectedSize) { return delegated(); }

    public <T> UpdateResult<U, Stream<T>> fetchStream(Class<T> elementClass) { return delegated(); }

    public <T, PP extends PreparedStatement> UpdateResult<U, T> fetch(ManagedAction<PP, R, T> next) { return delegated(); }

    public <T> UpdateResult<U, T> fetchOutParameters(Class<T> toClass) { return delegated(); }
}
