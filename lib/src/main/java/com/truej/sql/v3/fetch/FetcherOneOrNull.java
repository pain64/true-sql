package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherOneOrNull<T> implements
    ToPreparedStatement.ManagedAction<@Nullable T>,
    FetcherUpdateCount.Next<@Nullable T>,
    FetcherGeneratedKeys.Next<@Nullable T> {

    public static class Hints { }

    public static <T> @Nullable T apply(
        ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        var iterator = mapper.map(rs);

        if (iterator.hasNext()) {
            var result = iterator.next();
            if (iterator.hasNext())
                throw new TooMuchRowsException();
            return result;
        }

        return null;
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherOneOrNull(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return false;
    }
    @Override public @Nullable T apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public @Nullable T apply(Concrete source) throws SQLException {
        return apply(source.rs, mapper);
    }

    public static <T> @Nullable T apply(
        Concrete source, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        return apply(source.rs, mapper);
    }

    public interface Instance extends ToPreparedStatement {
        default <T> @Nullable T fetchOneOrNull(
            Source source, ResultSetMapper<T, Hints> mapper
        ) {
            return managed(source, new FetcherOneOrNull<>(mapper));
        }
    }
}
