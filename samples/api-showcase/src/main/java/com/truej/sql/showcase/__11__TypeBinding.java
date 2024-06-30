package com.truej.sql.showcase;

import com.truej.sql.v3.bindings.AsObjectReadWrite;
import com.truej.sql.v3.config.Configuration;
import com.truej.sql.v3.config.TypeBinding;
import com.truej.sql.v3.config.TypeReadWrite;
import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;

import javax.sql.DataSource;
import java.sql.*;

public class __11__TypeBinding {

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

    enum UserSex {MALE, FEMALE}
    class PgUserSexRW extends PgEnumRW<UserSex> {
        @Override public Class<UserSex> aClass() { return UserSex.class; }
    }

    class PGpoint { } // in pg jdbc library
    class PgPointRW extends AsObjectReadWrite<PGpoint> { }

    @Configuration(
        typeBindings = {
            @TypeBinding(
                compatibleSqlType = Types.OTHER,
                compatibleSqlTypeName = "user_sex",
                rw = PgUserSexRW.class
            ),
            @TypeBinding(
                compatibleSqlType = Types.OTHER,
                compatibleSqlTypeName = "point",
                rw = PgPointRW.class
            ),
        }
    ) record PgDb(DataSource w) implements DataSourceW { }

    void useIt(PgDb ds) {
        ds.q("select sex from users").fetchList(UserSex.class);
    }

    // trololo with MySQL

    abstract class MySqlEnumRW<T extends Enum<T>> implements TypeReadWrite<T> {

        public abstract Class<T> aClass();

        @Override public T get(
            ResultSet rs, int columnIndex
        ) throws SQLException {
            return Enum.valueOf(aClass(), rs.getString(columnIndex));
        }

        @Override public void set(
            PreparedStatement stmt, int parameterIndex, T value
        ) throws SQLException {
            stmt.setString(parameterIndex, value.name());
        }

        @Override public T get(
            CallableStatement stmt, int parameterIndex
        ) throws SQLException {
            return Enum.valueOf(aClass(), stmt.getString(parameterIndex));
        }

        @Override public void registerOutParameter(
            CallableStatement stmt, int parameterIndex
        ) throws SQLException {
            stmt.registerOutParameter(parameterIndex, Types.VARCHAR);
        }
    }

    class MySqlUserSexRW extends PgEnumRW<UserSex> {
        @Override public Class<UserSex> aClass() { return UserSex.class; }
    }

    @Configuration(
        typeBindings = {
            @TypeBinding(
                compatibleSqlType = Types.VARCHAR,
                compatibleSqlTypeName = "varchar",
                rw = MySqlUserSexRW.class
            )
        }
    ) record MySqlDb(DataSource w) implements DataSourceW { }

    // generated
    record User(String name, UserSex sex) { }

    void useItWithGAndTypeCast(PgDb ds) {

        // as like as PG
        ds.q("select sex from users").fetchList(UserSex.class);

        // type hint required!
        ds.q("""
            select name, sex as ":t UserSex" from users"""
        ).g.fetchList(User.class);
    }
}
