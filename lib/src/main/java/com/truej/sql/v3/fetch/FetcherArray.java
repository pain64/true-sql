package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FetcherArray {
//    // 2 versions: Simple & WithUpdateCount
//    public static <T> T[] fetch(ResultSet rs, ResultSetMapper<T> mapper) {
//        try {
//
//        } finally {
//            rs.close();
//        }
//
//        // TODO: wrap to try-with-resources
//        //noinspection unchecked
//        return (T[]) FetcherStream.fetch(rs, mapper).toArray();
//    }

    public interface Default extends ToPreparedStatement {
        default <T> T[] fetchArray(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, mapper));
        }

        default <T> T[] fetchArray(Connection cn, ResultSetMapper<T> mapper) {
            // TODO: actual implementation
            return null;
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<T[]> fetchArray(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, mapper));
        }

        default <T> UpdateResult<T[]> fetchArray(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchArray(cn, mapper));
        }
    }
}
