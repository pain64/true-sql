package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FetcherOne<T> implements
    ToPreparedStatement.ManagedAction<T>,
    FetcherUpdateCount.Next<T>,
    FetcherGeneratedKeys.Next<T> {

    public static class Hints { }

    public static <T> T apply(
        ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        var iterator = mapper.map(rs);

        if (iterator.hasNext()) {
            var result = iterator.next();
            if (iterator.hasNext())
                throw new TooMuchRowsException();
            return result;
        }

        throw new TooFewRowsException();
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherOne(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return false;
    }
    @Override public T apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public T apply(Concrete source) throws SQLException {
        return apply(source.rs, this.mapper);
    }

    public interface Instance extends ToPreparedStatement {
        default <T> T fetchOne(Source source, ResultSetMapper<T, Hints> mapper) {
            return managed(source, new FetcherOne<>(mapper));
        }
    }
}
