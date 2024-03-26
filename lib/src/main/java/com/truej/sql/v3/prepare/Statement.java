package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Statement
    extends StatementSettings<Statement>
    implements ToPreparedStatement,
    FetcherManual.Instance, FetcherNone.Instance,
    FetcherOne.Instance, FetcherOneOptional.Instance, FetcherOneOrNull.Instance,
    FetcherList.Instance, FetcherStream.Instance,
    FetcherUpdateCount.Instance, FetcherGeneratedKeys.Instance {

    @Override Statement self() { return this; }

    @Override void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
    }
}
