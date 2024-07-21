package net.truej.sql.dsl;

import net.truej.sql.source.Parameters;

import java.util.List;

import static net.truej.sql.dsl.MissConfigurationException.*;

public interface Q<S, B> {
// TO BE DONE:
//    interface BatchTemplateExtractor<T> {
//        StringTemplate extract(T one);
//    }
//    default void q(StringTemplate query) { raise(); }
//    default <T> void q(List<T> batch, BatchTemplateExtractor<T> query) { raise(); }

    default S q(String query, Object... args) { return raise(); }
    default <T> B q(List<T> batch, String query, Parameters.BatchArgumentsExtractor<T> arguments) { return raise(); }
}
