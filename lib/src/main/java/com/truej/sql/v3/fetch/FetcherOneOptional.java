package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Optional;

public class FetcherOneOptional {

    public interface Default extends ToPreparedStatement {
        default <T> Optional<T> fetchOneOptional(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
        }

        default <T> Optional<T> fetchOneOptional(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return Optional.ofNullable(
                ((FetcherOneOrNull.Default) self::prepare).fetchOneOrNull(cn, mapper)
            );
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<Optional<T>> fetchOneOptional(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
        }

        default <T> UpdateResult<Optional<T>> fetchOneOptional(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchOneOptional(cn, mapper));
        }
    }
}
