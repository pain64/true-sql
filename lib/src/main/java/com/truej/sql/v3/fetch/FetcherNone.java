package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherNone {
    public static Void apply(ResultSet rs) {
        return null;
    }

    public static Void apply(PreparedStatement stmt) {
        return null;
    }

    public interface Instance extends ToPreparedStatement {
        default Void fetchNone(DataSource ds) {
            return TrueSql.withConnection(ds, this::fetchNone);
        }

        default Void fetchNone(Connection cn) {
            try (var __ = prepareAndExecute(cn)) {
                return null;
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
