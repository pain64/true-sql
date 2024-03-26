package com.truej.sql.v3;

import java.sql.Connection;

public interface ConnectionW extends Source {
    Connection w();

    // FIXME: needs to be private API???
    @Override default <T, E extends Exception> T withConnection(
        WithConnectionAction<T, E> action
    ) throws E {
        return action.run(this);
    }

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
