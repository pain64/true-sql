package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class StatementWithGeneratedKeys implements ToPreparedStatement,
    FetcherOne.Default, FetcherOneOptional.Default, FetcherOneOrNull.Default,
    FetcherArray.Default, FetcherList.Default,
    FetcherStream.Default, FetcherManual.Default {

    public StatementWithGeneratedKeysAndUpdateCount withUpdateCount() {
        return withUpdateCount(false);
    }

    public StatementWithGeneratedKeysAndUpdateCount withUpdateCount(boolean isLarge) {
        return null;
    }

    @Override public PreparedStatement prepare() {
        return null;
    }
}
