package com.truej.sql.v3.source;

import java.sql.Connection;

public non-sealed interface ConnectionW extends Source {
    Connection w();

    interface InTransactionAction<T, E extends Exception> {
        T run() throws E;
    }

    default  <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        // TODO
        return action.run();
    }
}
