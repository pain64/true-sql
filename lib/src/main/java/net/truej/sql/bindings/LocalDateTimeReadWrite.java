package net.truej.sql.bindings;

import java.sql.Types;
import java.time.LocalDateTime;

public class LocalDateTimeReadWrite extends AsObjectReadWrite<LocalDateTime> {
    @Override public int sqlType() { return Types.TIMESTAMP; }
    @Override public Class<LocalDateTime> aClass() { return LocalDateTime.class; }
}
