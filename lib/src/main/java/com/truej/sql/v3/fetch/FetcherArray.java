package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import java.sql.ResultSet;

public class FetcherArray {
    public static <T> T[] fetch(ResultSet rs, ResultSetMapper<T> mapper) {
        //noinspection unchecked
        return (T[]) FetcherStream.fetch(rs, mapper).toArray();
    }
}
