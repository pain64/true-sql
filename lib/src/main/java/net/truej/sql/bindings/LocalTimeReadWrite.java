package net.truej.sql.bindings;

import java.sql.Types;
import java.time.LocalTime;

public class LocalTimeReadWrite extends AsObjectReadWrite<LocalTime> {
    @Override public int sqlType() { return Types.TIME; }
    @Override public Class<LocalTime> aClass() { return LocalTime.class; }
}
