package net.truej.sql.bindings;

import java.sql.Types;
import java.time.OffsetDateTime;

public class OffsetDateTimeReadWrite extends AsObjectReadWrite<OffsetDateTime> {
    @Override public int sqlType() { return Types.TIMESTAMP_WITH_TIMEZONE; }
    @Override public Class<OffsetDateTime> aClass() { return OffsetDateTime.class; }
}
