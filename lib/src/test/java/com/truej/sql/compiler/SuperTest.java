package com.truej.sql.compiler;

import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.config.Configuration;
import com.truej.sql.v3.config.TypeBinding;
import com.truej.sql.v3.config.TypeReadWrite;
import com.truej.sql.v3.source.ConnectionW;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Type;
import java.sql.*;


// FIXME: does not works if this record will be in class or method
// VS Nullable ??? May type be nullable ???
abstract class PgEnumRW<T extends Enum<T>> implements TypeReadWrite<T> {

    public abstract Class<T> aClass();

    @Override public T get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), rs.getString(columnIndex));
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, T value
    ) throws SQLException {
        stmt.setObject(parameterIndex, value, Types.OTHER);
    }

    @Override public T get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), stmt.getString(parameterIndex));
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.OTHER);
    }
}

// User <-> varchar
interface TypeConversion<T> extends TypeReadWrite<T> {

}

enum UserSex {MALE, FEMALE}
class UserSexRW extends PgEnumRW<UserSex> {
    @Override public Class<UserSex> aClass() { return UserSex.class; }
}

@Configuration(
    typeBindings = {
        @TypeBinding(
            compatibleSqlType = Types.OTHER,
            compatibleSqlTypeName = "user_sex",
            rw = UserSexRW.class
        ),
        @TypeBinding(
            compatibleSqlType = Types.OTHER,
            compatibleSqlTypeName = "int",
            rw = UserSexRW.class
        )
    }
)
record MainConnection(Connection w) implements ConnectionW { }

interface ParameterSetter<T> {
    void set(PreparedStatement stmt, int i, T v) throws SQLException;
}
@ExtendWith(TrueSqlTests.class)
@TrueSql public class SuperTest {

    @Test public void test1() {
        ParameterSetter<Long> x = PreparedStatement::setLong;

//        var cn = new MainConnection(null);
//        cn."select id from users".fetchOne(Long.class);
        Assertions.assertEquals(1, 1);
    }
}
