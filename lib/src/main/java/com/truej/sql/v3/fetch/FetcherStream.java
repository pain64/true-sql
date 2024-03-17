package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

public class FetcherStream {
    public static class Hints { }

    public static <T> FetcherGeneratedKeys.Next<Stream<T>> forResultSet(
        ResultSetMapper<T, Hints> mapper
    ) {
        return new FetcherGeneratedKeys.Next<>() {

            @Override public boolean isPreparedStatementMoved() {
                return true;
            }

            @Override public Stream<T> apply(ResultSet rs) {
                final Iterator<T> iterator;
                try {
                    iterator = mapper.map(rs);
                } catch (Exception e) {
                    try {
                        rs.getStatement().close();
                    } catch (SQLException closeError) {
                        e.addSuppressed(closeError);
                    }

                    throw e;
                }

                return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false
                ).onClose(() -> {
                    try {
                        rs.getStatement().close();
                    } catch (SQLException e) {
                        throw new SqlExceptionR(e);
                    }
                });
            }
        };
    }

    public static <T> FetcherUpdateCount.Next<Stream<T>> forStatement(
        ResultSetMapper<T, Hints> mapper
    ) {
        return new FetcherUpdateCount.Next<>() {

            @Override public boolean isPreparedStatementMoved() {
                return FetcherUpdateCount.Next.super.isPreparedStatementMoved();
            }

            @Override public Stream<T> apply(PreparedStatement stmt) {
                try {
                    return forResultSet(mapper).apply(stmt.getResultSet());
                } catch (SQLException e) {
                    throw new SqlExceptionR(e);
                }
            }
        };
    }

    public interface Instance extends ToPreparedStatement {
        default <T> Stream<T> fetchStream(DataSource ds, ResultSetMapper<T, Hints> mapper) {
            return TrueSql.withConnection(ds, cn -> fetchStream(cn, mapper));
        }

        default <T> Stream<T> fetchStream(Connection cn, ResultSetMapper<T, Hints> mapper) {
            final PreparedStatement stmt;
            try {
                stmt = prepareAndExecute(cn);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }

            return forStatement(mapper).apply(stmt);
        }
    }
}
