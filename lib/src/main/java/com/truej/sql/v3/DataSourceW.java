package com.truej.sql.v3;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface DataSourceW extends Source {
    DataSource w();
    @Override default <T, E extends Exception> T withConnection(
        WithConnectionAction<T, E> action
    ) throws E {
        try (var cn = w().getConnection()) {
            return action.run(() -> cn);
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    interface InTransactionAction<T, E extends Exception> {
        T run(ConnectionW connection) throws E;
    }

    default <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        return withConnection(
            conn -> conn.inTransaction(() -> action.run(conn))
        );
    }
}
