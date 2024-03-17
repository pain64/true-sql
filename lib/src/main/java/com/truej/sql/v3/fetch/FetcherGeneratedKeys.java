package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherGeneratedKeys {

    public interface Next<T> {
        default boolean isPreparedStatementMoved() { return false; }
        T apply(ResultSet rs);
    }

    public static <T> T apply(PreparedStatement stmt, Next<T> next) {
        try {
            return next.apply(stmt.getGeneratedKeys());
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> T fetchGeneratedKeys(DataSource ds, Next<T> next) {
            return TrueSql.withConnection(ds, cn -> fetchGeneratedKeys(cn, next));
        }

        default <T> T fetchGeneratedKeys(Connection cn, Next<T> next) {
            try (var stmt = prepareAndExecute(cn)) {
                return apply(stmt, next);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
