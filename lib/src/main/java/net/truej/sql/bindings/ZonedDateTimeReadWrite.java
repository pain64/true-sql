package net.truej.sql.bindings;

import java.time.ZonedDateTime;

public class ZonedDateTimeReadWrite extends AsObjectReadWrite<ZonedDateTime> {
    @Override public Class<ZonedDateTime> aClass() { return ZonedDateTime.class; }
}
