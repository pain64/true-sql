package net.truej.sql.bindings;

import java.time.LocalDateTime;

public class LocalDateTimeReadWrite extends AsObjectReadWrite<LocalDateTime> {
    @Override public Class<LocalDateTime> aClass() { return LocalDateTime.class; }
}
