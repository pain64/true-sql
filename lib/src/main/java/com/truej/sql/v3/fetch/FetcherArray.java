package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class FetcherArray {
    public static class Hints {
        private int expectedSize = 0;

        public Hints expectedSize(int n) {
            this.expectedSize = n;
            return this;
        }
    }

    public static <T> T[] apply(ResultSet rs, ResultSetMapper<T, Hints> mapper) {
        var hints = mapper.hints();

        //noinspection unchecked
        return FetcherList.apply(rs, new ResultSetMapper<T, FetcherList.Hints>() {
            @Override public Class<T> tClass() {
                return mapper.tClass();
            }
            @Override public @Nullable FetcherList.Hints hints() {
                return hints == null ? null
                    : new FetcherList.Hints().expectedSize(hints.expectedSize);
            }
            @Override public Iterator<T> map(ResultSet rs) {
                return mapper.map(rs);
            }
        }).toArray(n -> (T[]) Array.newInstance(mapper.tClass(), n));
    }

    public static <T> T[] apply(PreparedStatement stmt, ResultSetMapper<T, Hints> mapper) {
        try {
            var rs = stmt.getResultSet();
            return apply(rs, mapper);
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> T[] fetchArray(DataSource ds, ResultSetMapper<T, Hints> mapper) {
            return TrueSql.withConnection(ds, cn -> fetchArray(cn, mapper));
        }

        default <T> T[] fetchArray(Connection cn, ResultSetMapper<T, Hints> mapper) {
            try (var stmt = prepareAndExecute(cn)) {
                return apply(stmt, mapper);
            } catch (SQLException e) {
                // TODO: convert to ConstraintViolationException possible here???
                throw new SqlExceptionR(e);
            }
        }
    }
}
