package net.truej.sql.compiler;

import net.truej.sql.bindings.AsObjectReadWrite;
import org.postgresql.geometric.PGpoint;

public class PgPointRW extends AsObjectReadWrite<PGpoint> {
    @Override public Class<PGpoint> aClass() { return PGpoint.class; }
}