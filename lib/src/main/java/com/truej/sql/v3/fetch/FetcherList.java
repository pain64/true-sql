package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FetcherList<T> implements
    FetcherUpdateCount.Next<List<T>>, FetcherGeneratedKeys.Next<List<T>> {

    public static class Hints {
        private int expectedSize = 0;

        public Hints expectedSize(int n) {
            this.expectedSize = n;
            return this;
        }
    }

    public static <T> List<T> apply(
        ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        var hints = mapper.hints();
        var iterator = mapper.map(rs);

        var result = hints != null && hints.expectedSize != 0
            ? new ArrayList<T>(hints.expectedSize)
            : new ArrayList<T>();

        while (iterator.hasNext())
            result.add(iterator.next());

        return result;
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherList(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return false;
    }
    @Override public List<T> apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public List<T> apply(Concrete source) throws SQLException {
        return apply(source.rs, this.mapper);
    }

    public interface Instance extends ToPreparedStatement {
        default <T> List<T> fetchList(Source source, ResultSetMapper<T, Hints> mapper) {
            return managed(
                // FIXME: deduplicate
                source, () -> false, stmt -> new FetcherList<>(mapper).apply(stmt)
            );
        }
    }
}
