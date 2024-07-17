package net.truej.sql.bindings;

import java.time.LocalDate;

public class LocalDateReadWrite extends AsObjectReadWrite<LocalDate> {
    @Override public Class<LocalDate> aClass() { return LocalDate.class; }
}
