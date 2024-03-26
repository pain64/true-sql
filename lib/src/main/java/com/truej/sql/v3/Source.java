package com.truej.sql.v3;

public interface Source {
    interface WithConnectionAction<T, E extends Exception> {
        T run(ConnectionW cn) throws E;
    }

    <T, E extends Exception> T withConnection(WithConnectionAction<T, E> action) throws E;
}
