package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class FetcherOneOptional {
    public static class Hints { }

    public static <T> Optional<T> apply(ResultSet rs, ResultSetMapper<T, Hints> mapper) {
        return Optional.ofNullable(
            FetcherOneOrNull.apply(rs, new ResultSetMapper<>() {
                @Override public Class<T> tClass() { return mapper.tClass(); }
                @Override public @Nullable FetcherOneOrNull.Hints hints() { return null; }
                @Override public Iterator<T> map(ResultSet rs) { return mapper.map(rs); }
            })
        );
    }

    public static <T> Optional<T> apply(PreparedStatement stmt, ResultSetMapper<T, Hints> mapper) {
        try {
            var rs = stmt.getResultSet();
            return apply(rs, mapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> Optional<T> fetchOneOptional(DataSource ds, ResultSetMapper<T, Hints> mapper) {
            return TrueSql.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
        }

        default <T> Optional<T> fetchOneOptional(Connection cn, ResultSetMapper<T, Hints> mapper) {
            try (var stmt = prepareAndExecute(cn)) {
                return apply(stmt, mapper);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
