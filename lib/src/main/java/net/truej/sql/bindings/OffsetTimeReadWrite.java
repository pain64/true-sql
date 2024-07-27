package net.truej.sql.bindings;

import java.sql.Types;
import java.time.OffsetTime;

public class OffsetTimeReadWrite extends AsObjectReadWrite<OffsetTime> {
    @Override public int sqlType() { return Types.TIME_WITH_TIMEZONE; }
    @Override public Class<OffsetTime> aClass() { return OffsetTime.class; }
}
