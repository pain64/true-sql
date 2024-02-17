package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import java.sql.ResultSet;

public class FetcherOne {
    public static <T> T fetch(ResultSet rs, ResultSetMapper<T> mapper) {
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
