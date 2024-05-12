package com.truej.sql.v3.prepare;

import java.sql.PreparedStatement;

public abstract class FetchApi<P extends PreparedStatement, R, U> extends FetchApi0<P, R, U> {
    public final FetchApiUpdateCount<P, R, U> withUpdateCount = new FetchApiUpdateCount<>();
    public final FetchApi0<P, R, U> g = this;
}
