package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

public class FetcherOne {

    public interface Default extends ToPreparedStatement {
        default <T> T fetchOne(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
        }

        default <T> T fetchOne(Connection cn, ResultSetMapper<T> mapper) {
            ResultSet rs = null;
            var iterator = mapper.map(rs);

            if (iterator.hasNext()) {
                var result = iterator.next();
                if (iterator.hasNext())
                    throw new TooMuchRowsException();
                return result;
            }

            throw new TooFewRowsException();
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<T> fetchOne(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
        }

        default <T> UpdateResult<T> fetchOne(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchOne(cn, mapper));
        }
    }
}
