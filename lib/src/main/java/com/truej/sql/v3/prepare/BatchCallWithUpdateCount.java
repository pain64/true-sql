package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BatchCallWithUpdateCount implements
    ToPreparedStatement, FetcherNone,
    FetcherArray, FetcherList, FetcherStream, FetcherManual {

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }
}