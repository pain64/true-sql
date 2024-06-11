package com.truej.sql.v3.prepare;

import java.sql.PreparedStatement;

public abstract class FetchApi<P extends PreparedStatement, R, U> extends FetchApi0<P, R, U> {
    public final FetchApiUpdateCount<P, R, U> withUpdateCount = new FetchApiUpdateCount<>();
    /**
     * Tells TrueSql to generate Dto
     */
    public final FetchApi0<P, R, U> g = this;

    public Void fetchNone() { return delegated(); }

    public <T, PP extends PreparedStatement> T fetch(ManagedAction<PP, R, T> next) { return delegated(); }
}
