package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.FetcherManual;
import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.fetch.ToPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class StatementWithUpdateCount implements
    ToPreparedStatement, FetcherNone, FetcherManual {

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }
}
