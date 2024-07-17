package net.truej.sql.bindings;

import java.time.OffsetTime;

public class OffsetTimeReadWrite extends AsObjectReadWrite<OffsetTime> {
    @Override public Class<OffsetTime> aClass() { return OffsetTime.class; }
}
