package net.truej.sql.config;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeReadWrite<T> {
     T get(ResultSet rs, int columnIndex) throws SQLException;
     void set(PreparedStatement stmt, int parameterIndex, T value) throws SQLException;
     T get(CallableStatement stmt, int parameterIndex) throws SQLException;
     void registerOutParameter(CallableStatement stmt, int parameterIndex) throws SQLException;
}
