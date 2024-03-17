package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FetcherList {
    public static class Hints {
        int expectedSize = 0;

        public Hints() { }

        public Hints(int expectedSize) {
            this.expectedSize = expectedSize;
        }

        public Hints expectedSize(int n) {
            this.expectedSize = n;
            return this;
        }
    }

    public static <T> List<T> apply(ResultSet rs, ResultSetMapper<T, Hints> mapper) {
        var hints = mapper.hints();
        var iterator = mapper.map(rs);

        var result = hints != null && hints.expectedSize != 0
            ? new ArrayList<T>(hints.expectedSize)
            : new ArrayList<T>();

        while (iterator.hasNext())
            result.add(iterator.next());

        return result;
    }

    public static <T> List<T> apply(PreparedStatement stmt, ResultSetMapper<T, Hints> mapper) {
        try {
            var rs = stmt.getResultSet();
            return apply(rs, mapper);
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> List<T> fetchList(DataSource ds, ResultSetMapper<T, Hints> mapper) {
            return TrueSql.withConnection(ds, cn -> fetchList(cn, mapper));
        }

        default <T> List<T> fetchList(Connection cn, ResultSetMapper<T, Hints> mapper) {
            try (
                var stmt = prepareAndExecute(cn)
            ) {
                return apply(stmt, mapper);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
