package net.truej.sql.compiler;

import net.truej.sql.bindings.AsObjectReadWrite;
import org.postgresql.geometric.PGpoint;

// FIXME: remove?
public class PgPointNotNullableRW extends AsObjectReadWrite<PGpoint> {
    @Override public Class<PGpoint> aClass() { return PGpoint.class; }
}
