package net.truej.sql.dsl;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static net.truej.sql.dsl.MissConfigurationException.*;
import static net.truej.sql.source.Parameters.*;

public interface NoUpdateCount {
    interface None {
        default Void fetchNone() { return raise(); }
    }

    interface OneG {
        default <T> T fetchOne(Class<T> toClass) { return raise(); }
    }

    interface One extends OneG {
        default <T> @Nullable T fetchOne(AsNullable asNullable, Class<T> toClass) { return raise(); }
        default <T> T fetchOne(AsNotNull asNotNull, Class<T> toClass) { return raise(); }
    }

    interface OneOrZeroG {
        default <T> @Nullable T fetchOneOrZero(Class<T> toClass) { return raise(); }
    }

    interface OneOrZero extends OneOrZeroG {
        default <T> @Nullable T fetchOneOrZero(AsNullable asNullable, Class<T> toClass) { return raise(); }
        default <T> @Nullable T fetchOneOrZero(AsNotNull asNotNull, Class<T> toClass) { return raise(); }
    }

    interface ListG {
        default <T> List<T> fetchList(Class<T> elementClass) { return raise(); }
    }

    interface List_ extends ListG {
        default <T> List<@Nullable T> fetchList(AsNullable asNullable, Class<T> elementClass) { return raise(); }
        default <T> List<T> fetchList(AsNotNull asNotNull, Class<T> elementClass) { return raise(); }
    }

    interface StreamG {
        default <T> Stream<T> fetchStream(Class<T> elementClass) { return raise(); }
    }

    interface Stream_ extends StreamG {
        default <T> Stream<@Nullable T> fetchStream(AsNullable asNullable, Class<T> elementClass) { return raise(); }
        default <T> Stream<T> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return raise(); }
    }
}
