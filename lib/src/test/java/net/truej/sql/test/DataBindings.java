package net.truej.sql.test;

import net.truej.sql.bindings.AsObjectReadWrite;
import org.postgresql.geometric.PGpoint;

public class DataBindings {
    public class PgPointRW extends AsObjectReadWrite<PGpoint> {
        @Override public Class<PGpoint> aClass() { return PGpoint.class; }
    }

}
