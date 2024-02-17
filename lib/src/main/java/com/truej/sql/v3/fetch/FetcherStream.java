package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import java.sql.ResultSet;
import java.util.stream.Stream;

public class FetcherStream {
    public static <T> Stream<T> fetch(ResultSet rs, ResultSetMapper<T> mapper) {
        return null;
    }
}
