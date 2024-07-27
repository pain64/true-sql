package net.truej.sql.bindings;

import java.sql.Types;
import java.time.ZonedDateTime;

public class ZonedDateTimeReadWrite extends AsObjectReadWrite<ZonedDateTime> {
    @Override public int sqlType() { return Types.TIMESTAMP_WITH_TIMEZONE; }
    @Override public Class<ZonedDateTime> aClass() { return ZonedDateTime.class; }
}
