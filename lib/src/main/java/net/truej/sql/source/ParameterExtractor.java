package net.truej.sql.source;

public interface ParameterExtractor<B, P> {
    P get(B batchElement);
}
