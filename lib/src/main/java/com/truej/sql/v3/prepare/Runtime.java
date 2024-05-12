package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Runtime {
    public static ResultSet getResultSet(
        PreparedStatement stmt, boolean hasGeneratedKeys
    ) throws SQLException {
        return hasGeneratedKeys ? stmt.getGeneratedKeys() : stmt.getResultSet();
    }

    private static <P extends PreparedStatement, R, U, T>
    T managed(Base<?, P, R, U> base, ConnectionW connection, ManagedAction<P, R, T> action) {
        try {
            var triedToClose = false;

            var stmt = base.prepare(connection.w());

            try {
                base.bindArgs(stmt);
                base.afterPrepare(stmt);
                var execResult = base.execute(stmt);

                return action.apply(connection, execResult, stmt, base.isAsGeneratedKeys());
            } catch (Exception e) {
                triedToClose = true;
                try {
                    stmt.close();
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                }

                throw e;
            } finally {
                if (!action.willStatementBeMoved() && !triedToClose)
                    stmt.close();
            }
        } catch (SQLException e) {
            throw connection.mapException(e);
        }
    }

    public static <P extends PreparedStatement, R, U, T>
    T managed(Base<?, P, R, U> base, ManagedAction<P, R, T> action) {
        return switch (base.source()) {
            case ConnectionW cn -> managed(base, cn, action);
            case DataSourceW ds -> ds.withConnection(cn -> managed(base, cn, action));
        };
    }
}
