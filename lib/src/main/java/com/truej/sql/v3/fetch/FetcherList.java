package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class FetcherList {
    public interface Default extends ToPreparedStatement {
        default <T> List<T> fetchList(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchList(cn, mapper));
        }

        default <T> List<T> fetchList(Connection cn, ResultSetMapper<T> mapper) {
            // TODO: actual implementation
            return null;
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<List<T>> fetchList(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchList(cn, mapper));
        }

        default <T> UpdateResult<List<T>> fetchList(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchList(cn, mapper));
        }
    }
}
