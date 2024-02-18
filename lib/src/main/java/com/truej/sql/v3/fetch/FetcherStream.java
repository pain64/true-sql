package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.stream.Stream;

public class FetcherStream {

    public interface Default extends ToPreparedStatement {
        default <T> Stream<T> fetchStream(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, mapper));
        }

        default <T> Stream<T> fetchStream(Connection cn, ResultSetMapper<T> mapper) {
           return null;
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<Stream<T>> fetchStream(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, mapper));
        }

        default <T> UpdateResult<Stream<T>> fetchStream(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchStream(cn, mapper));
        }
    }
}
