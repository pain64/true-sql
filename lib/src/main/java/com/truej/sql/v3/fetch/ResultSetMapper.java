package com.truej.sql.v3.fetch;

import java.sql.ResultSet;
import java.util.Iterator;

public interface ResultSetMapper<T> {
    Iterator<T> map(ResultSet rs);
}
