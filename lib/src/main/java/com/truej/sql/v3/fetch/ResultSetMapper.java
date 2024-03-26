package com.truej.sql.v3.fetch;

import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.Iterator;

public interface ResultSetMapper<T, H> {
    @Nullable H hints();
    Iterator<T> map(ResultSet rs);
}
