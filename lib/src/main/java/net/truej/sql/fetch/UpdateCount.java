package net.truej.sql.fetch;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.truej.sql.fetch.MissConfigurationException.*;
import static net.truej.sql.fetch.Parameters.*;

public interface UpdateCount {
    interface None<U> {
        default U fetchNone() { return raise(); }
    }

    interface OneG<U> {
        default <T> UpdateResult<U, T> fetchOne(Class<T> toClass) { return raise(); }
    }

    interface One<U> extends OneG<U> {
        default <T> UpdateResult<U, @Nullable T> fetchOne(AsNullable asNullable, Class<T> toClass) { return raise(); }
        default <T> UpdateResult<U, T> fetchOne(AsNotNull asNotNull, Class<T> toClass) { return raise(); }
    }

    interface OneOrZeroG<U> {
        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(Class<T> toClass) { return raise(); }
    }

    interface OneOrZero<U> extends OneOrZeroG<U> {
        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(AsNullable asNullable, Class<T> toClass) { return raise(); }
        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(AsNotNull asNotNull, Class<T> toClass) { return raise(); }
    }

    interface ListG<U> {
        default <T> UpdateResult<U, List<T>> fetchList(Class<T> elementClass) { return raise(); }
    }

    interface List_<U> extends ListG<U> {
        default <T> UpdateResult<U, List<@Nullable T>> fetchList(AsNullable asNullable, Class<T> elementClass) { return raise(); }
        default <T> UpdateResult<U, List<T>> fetchList(AsNotNull asNotNull, Class<T> elementClass) { return raise(); }
    }

    interface StreamG<U> {
        default <T> UpdateResultStream<U, T> fetchStream(Class<T> elementClass) { return raise(); }
    }

    interface Stream_<U> extends StreamG<U> {
        default <T> UpdateResultStream<U, @Nullable T> fetchStream(AsNullable asNullable, Class<T> elementClass) { return raise(); }
        default <T> UpdateResultStream<U, T> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return raise(); }
    }
}
