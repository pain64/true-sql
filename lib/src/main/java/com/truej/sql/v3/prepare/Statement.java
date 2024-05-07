package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.Source;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Statement
    extends Base.Single<Statement, PreparedStatement>
    implements With.GeneratedKeysConstructors<Statement> {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override Statement self() { return this; }

    @Override Void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }

    public StatementAsCall asCall() {
        var self = this;
        return new StatementAsCall() {
            @Override protected Source source() {
                return self.source();
            }
            @Override protected String query() {
                return self.query();
            }
            @Override protected void bindArgs(CallableStatement stmt) throws SQLException {
                self.bindArgs(stmt);
            }
        };
    }
}
