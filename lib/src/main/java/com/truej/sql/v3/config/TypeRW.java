package com.truej.sql.v3.config;

public interface TypeRW {
    // read: rs(1) -> MyEnum.valueOf(rs.getString(1)) -> TJava
    String generateRead(String sqlType, String javaClass, String source, int columnNumber);
    // write: JValue -> stmt.setXXX(1, v)
    String generateWrite(String sqlType, String javaClass, String destination, int columnNumber, String value);
}
