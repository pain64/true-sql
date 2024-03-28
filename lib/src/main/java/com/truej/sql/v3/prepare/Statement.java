package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Statement
    extends StatementSettings<Statement, Long>
    implements FetcherManual.Instance, FetcherNone.Instance,
    FetcherOne.Instance, FetcherOneOptional.Instance,
    FetcherOneOrNull.Instance, FetcherList.Instance,
    FetcherStream.Instance, FetcherUpdateCount.Instance<Long>,
    FetcherGeneratedKeys.Instance {

    @Override Statement self() { return this; }

    @Override void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
    }

    @Override public Long getUpdateCount(PreparedStatement stmt) throws SQLException {
        return stmt.getLargeUpdateCount();
    }
}
