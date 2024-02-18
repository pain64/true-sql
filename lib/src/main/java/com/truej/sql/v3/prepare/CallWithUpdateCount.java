package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class CallWithUpdateCount implements ToPreparedStatement,
    FetcherNone.UpdateCount, FetcherOne.UpdateCount, FetcherManual.UpdateCount {

    @Override public PreparedStatement prepare() {
        return null;
    }
}
