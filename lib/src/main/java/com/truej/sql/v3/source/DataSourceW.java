package com.truej.sql.v3.source;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public non-sealed interface DataSourceW extends Source {
    DataSource w();

    interface WithConnectionAction<T, E extends Exception> {
        T run(ConnectionW cn) throws E;
    }

    default <T, E extends Exception> T withConnection(
        WithConnectionAction<T, E> action
    ) throws E {
        var self = this;
        try (var cn = w().getConnection()) {
            return action.run(
                new ConnectionW() {
                    @Override public RuntimeException mapException(SQLException ex) {
                        return self.mapException(ex);
                    }
                    @Override public Connection w() {
                        return cn;
                    }
                }
            );
        } catch (SQLException e) {
            throw mapException(e);
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
