package net.truej.sql.fetch;

import java.util.List;

import static net.truej.sql.fetch.MissConfigurationException.*;

public interface Q<S, B> {
// TO BE DONE:
//    interface BatchTemplateExtractor<T> {
//        StringTemplate extract(T one);
//    }
//    default void q(StringTemplate query) { raise(); }
//    default <T> void q(List<T> batch, BatchTemplateExtractor<T> query) { raise(); }

    default S q(String query, Object... args) { return raise(); }
    default <T> B q(List<T> batch, String query, Parameters.ArgumentsExtractor<T> arguments) { return raise(); }
}
