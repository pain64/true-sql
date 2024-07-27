package net.truej.sql.bindings;

import java.sql.Types;
import java.time.LocalDate;

public class LocalDateReadWrite extends AsObjectReadWrite<LocalDate> {
    @Override public int sqlType() { return Types.DATE; }
    @Override public Class<LocalDate> aClass() { return LocalDate.class; }
}
