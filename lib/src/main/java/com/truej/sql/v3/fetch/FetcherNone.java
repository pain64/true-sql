package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherNone implements ToPreparedStatement.ManagedAction<Void> {
    public static Void apply(ResultSet rs) {
        return null;
    }

    @Override public Void apply(PreparedStatement stmt) throws SQLException {
        return apply(stmt.getResultSet());
    }

    @Override public boolean willPreparedStatementBeMoved() { return false; }

    public interface Instance extends ToPreparedStatement {
        default Void fetchNone(Source source) {
            return managed(source, new FetcherNone());
        }
    }
}
