package net.truej.sql.source;

// FIXME: make nested in Parameters ???
public interface ParameterExtractor<B, P> {
    P get(B batchElement);
}
