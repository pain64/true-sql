package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherOne {
    public static class Hints { }

    public static <T> T apply(ResultSet rs, ResultSetMapper<T, Hints> mapper) {
        var iterator = mapper.map(rs);

        if (iterator.hasNext()) {
            var result = iterator.next();
            if (iterator.hasNext())
                throw new TooMuchRowsException();
            return result;
        }

        throw new TooFewRowsException();
    }

    public static <T> T apply(PreparedStatement stmt, ResultSetMapper<T, Hints> mapper) {
        try {
            var rs = stmt.getResultSet();
            return apply(rs, mapper);
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> T fetchOne(DataSource ds, ResultSetMapper<T, Hints> mapper) {
            return TrueSql.withConnection(ds, cn -> fetchOne(cn, mapper));
        }

        default <T> T fetchOne(Connection cn, ResultSetMapper<T, Hints> mapper) {
            try (var stmt = prepareAndExecute(cn)) {
                return apply(stmt, mapper);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
