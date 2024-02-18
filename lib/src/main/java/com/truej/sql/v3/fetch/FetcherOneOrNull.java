package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

public class FetcherOneOrNull {

    public interface Default extends ToPreparedStatement {
        default <T> @Nullable T fetchOneOrNull(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, mapper));
        }

        default <T> @Nullable T fetchOneOrNull(Connection cn, ResultSetMapper<T> mapper) {
            ResultSet rs = null;
            var iterator = mapper.map(rs);

            if (iterator.hasNext()) {
                var result = iterator.next();
                if (iterator.hasNext())
                    throw new TooMuchRowsException();
                return result;
            }

            return null;
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T> UpdateResult<@Nullable T> fetchOneOrNull(DataSource ds, ResultSetMapper<T> mapper) {
            return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, mapper));
        }

        default <T> UpdateResult<@Nullable T> fetchOneOrNull(Connection cn, ResultSetMapper<T> mapper) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchOneOrNull(cn, mapper));
        }
    }
}
