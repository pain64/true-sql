package net.truej.sql.bindings;

import java.time.OffsetDateTime;

public class OffsetDateTimeReadWrite extends AsObjectReadWrite<OffsetDateTime> {
    @Override public Class<OffsetDateTime> aClass() { return OffsetDateTime.class; }
}
