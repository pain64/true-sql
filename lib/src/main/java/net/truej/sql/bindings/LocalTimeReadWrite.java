package net.truej.sql.bindings;

import java.time.LocalTime;

public class LocalTimeReadWrite extends AsObjectReadWrite<LocalTime> {
    @Override public Class<LocalTime> aClass() { return LocalTime.class; }
}
