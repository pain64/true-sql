package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import java.sql.ResultSet;
import java.util.Optional;

public class FetcherOneOptional {
    public static <T> Optional<T> fetch(ResultSet rs, ResultSetMapper<T> mapper) {
        return Optional.ofNullable(FetcherOneOrNull.fetch(rs, mapper));
    }
}
