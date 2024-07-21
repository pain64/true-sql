package net.truej.sql.dsl;

import net.truej.sql.fetch.UpdateResult;
import net.truej.sql.fetch.UpdateResultStream;
import net.truej.sql.source.Parameters;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static net.truej.sql.dsl.MissConfigurationException.*;
import static net.truej.sql.source.Parameters.*;

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
        default <T> UpdateResultStream<U, Stream<T>> fetchStream(Class<T> elementClass) { return raise(); }
    }

    interface Stream_<U> extends StreamG<U> {
        default <T> UpdateResultStream<U, Stream<@Nullable T>> fetchStream(AsNullable asNullable, Class<T> elementClass) { return raise(); }
        default <T> UpdateResultStream<U, Stream<T>> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return raise(); }
    }
}
