package com.truej.sql.showcase;

import com.truej.sql.showcase.NewApi.PrepareStatement;
import com.truej.sql.showcase.gen.ExampleDto;
import com.truej.sql.*;

import com.truej.sql.config.*;
import com.truej.sql.v3.Constraint;
import com.truej.sql.v3.ConstraintViolationException;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Blob;
import java.util.List;

import static com.truej.sql.showcase.NewApi.fetch;
import static com.truej.sql.showcase.NewApi.one;
import static com.truej.sql.v3.TrueJdbc.*;

// TODO:
//   1. Реализация
//   2. Тестовое приложение: паттерны и использование TrueSql Api + postgresql, демо с Spring Web
//   3. Покрытие тестами (True Sql)
//   4. CI / CD (github?)
//   5. Паттерны: Check mysql & new oracle versions
//     5.1 Вставка новой строки и получение сгенерированных колонок (id)
//     5.2 Обработка ошибок на констрейнтах
//     5.3 Affected Rows count
//     5.4 Батчинг
//   6. Документация
//   7. Публикация jar в maven репозиторий
//   8. Лицензирование - подобрать лицензию (Apache2 или LGPL или ...)
//   9. Api для того чтобы забрать результаты из Exec - returning, generated keys, rows affected
//   Void,
//   10. Передача Array параметров: String[], List<?>
//   11. Чтение Array: https://docs.oracle.com/javase/tutorial/jdbc/basics/array.html
//   12. Хранимые процедуры и OUT параметры - TrueSql.call()
//   13. Утечка Array - живут в границе транзакции
//
//   insert into users(name, age) values (?, ?)
//
// class ExampleDto {
//
// }
// import ExampleDto.*;
@GenDto
public class Example {

    static class VarcharReader implements Reader {
        @Override
        public Class<?>[] allowedToJavaClasses() {
            var x = String[][].class;
            return new Class[]{String.class};
        }

        @Override
        public Class<?> defaultToJavaClass() {
            return String.class;
        }

        @Override
        public String generateReadExpression(
            String columnNumber, Class<?> toJavaClass
        ) {
            return StringTemplate.STR."rs.getString(\{columnNumber})";
        }
    }

    static class BlobWriter implements Writer {
        @Override
        public String[] allowedSqlTypes() {
            return new String[]{"blob"};
        }
        // default

        @Override
        public String generateWriteStatement(
            String fieldSelector, /*FIXME */ @Nullable String toSqlType
        ) {
            return StringTemplate.STR."rs.setBlob(\{fieldSelector})";
        }
    }

    static class StringDefaultReader implements DefaultReader {
        @Override
        public String generateReadExpression(String columnNumber) {
            return StringTemplate.STR."rs.getString(\{columnNumber})";
        }
    }

    // TODO: implement standard readers & writers
    @Database(
        name = "main",
        writeJavaClassMappings = {
            // Также, тут должна быть заложена схема проверки типов
            // всегда знаем javaClass
            @WriteJavaClass(javaClass = Blob.class, writer = BlobWriter.class),
            @WriteJavaClass(javaClass = String[].class, writer = StringArrayWriter.class),
            @WriteJavaClass(javaClass = Byte[].class, writer = StringArrayWriter.class),
            @WriteJavaClass(javaClass = byte[].class, writer = StringArrayWriter.class),
        },
        readSqlTypeMappings = {
            // varchar -> String
            @ReadSqlType(typeName = "varchar", reader = VarcharReader.class),
        },
        // В случае, если драйвер не предоставляет типов ResultSet
        readJavaClassMappings = {
            @ReadJavaClass(javaClass = String.class, reader = StringDefaultReader.class)
        }
    )
    static class MainDatabase extends TrueSql {
        MainDatabase(DataSource ds) {
            super(ds, e -> {
                // check SQL state
                // cast to Pg exception
                // new ConstraintViolationException(e.tableName, e.constraintName)
                return e;
            });
        }
    }

    @Database(name = "oracle")
    static class OracleDatabase extends TrueSql {
        OracleDatabase(DataSource ds) {
            super(ds);
        }
    }

    ExampleDto.User testSelect(MainDatabase db, long userId) {
        return db.queryOne(
            TrueSql.g(ExampleDto.User.class), TrueSql.SQL."select * from users where type = \{userId}"
        );
    }

    sealed interface UserInsertResponse permits Ok, NotUniqueName, BadEmail {
    }

    record Ok() implements UserInsertResponse {
    }

    record NotUniqueName() implements UserInsertResponse {
    }

    record BadEmail(String email) implements UserInsertResponse {
    }

    UserInsertResponse testInsert(
        MainDatabase database, long id, String name, String email
    ) {
        try {
            // TODO: returning vs generated keys vs affected rows
            // insert returning id;
            // Void.class
            // AffectedRows.class - всегда возвращаем
            // MyResult
            // class Result<T> {
            //    int affectedRows;
            //    T   value
            // }
            // exec:        Result<T> ~ MyValue.class ~ Void.class
            // execBatched: Result<List<T>>
            // MyValue {
            //     если только returning - просто имя поля
            //     если только generatedKeys - просто имя поля
            //     если и то и то:
            //        @Nullable returningName
            //        returningNewPrice
            //        generatedFieldName
            // }
            //
            // exec - affected rows
            // execReturning - (affectedRows, T)
            // execGeneratedKeys - (affectedRows, T)
            //
            // execBatch - affected rows
            // execBatchReturning - (affectedRows, List<T>)
            // execBatchGeneratedKeys - (affectedRows, List<T>)
            // .x()
            // .x

            var result = database.exec(
                TrueSql.g(MyValue.class), TrueSql.SQL."""
                insert into users(name, email) values (\{id}, \{name}, \{email}})"""
            );
            return new Ok();
        } catch (ConstraintViolationException e) {
            return e.when(
                new Constraint<>("users", "name_unique", NotUniqueName::new),
                new Constraint<>("users", "email_check", () -> {
                    // some logic
                    return new BadEmail(email);
                })
            );
        }
    }

    record UserData(long id, String name, String email) {
    }

    void testBatchInsert(MainDatabase database, List<UserData> users) {
        database.execBatched(Void.class, users, u ->
            TrueSql.SQL."""
                insert into users(id, name, email)
                    values(\{u.id}, \{u.name}, \{u.email})
                    on conflict do update set
                       name = EXCLUDED.name,
                       age  = EXCLUDED.age
                """
        );
    }

    void testSelectGrouped(DataSource ds) {
        var clinics = Stmt."""
                select
                    c.id        as "id                    ",
                    c.name      as "name                  ",
                    user.id     as ".id@patients          ",
                    user.name   as ".name                 ",
                    bank.id     as "..id@banks            ",
                    bank.money  as "..money               ",
                    doctor.id   as ".id@doctors           ",
                    doctor.name as ".name                 "
                from clinics c
                inner join doctors d on d.clinic_id = c.id
                inner join users   u on u.clinic_id = c.id
                inner join banks   b on b.user_id   = u.id
            """.fetchOne(ds, m(Clinics.class));

        var clinics = Stmt."""
                select
                    c.id        as "id                    ",
                    c.name      as "name                  ",
                    user.id     as ".id@Patient<> patients",
                    user.name   as ".name                 ",
                    bank.id     as "..id@Bank<> banks     ",
                    bank.money  as "..money               ",
                    doctor.id   as ".id@Doctor<> doctors  ",
                    doctor.name as ".name                 "
                from clinics c
                inner join doctors d on d.clinic_id = c.id
                inner join users   u on u.clinic_id = c.id
                inner join banks   b on b.user_id   = u.id
            """.fetchOne(ds, g(Clinics.class));
    }

    // truej.sql.DataSource<Database>
    // truej.sql.Connection<Database>

    // For prepare we need database
    // TODO: get unsafe mode

    void testMultipleDatabase(MainDatabase db) {
        // -- FOR SHORT DEMO
        // @Database("replica") var ds = ...
        // var users = Stmt."select * from users".<@G User>fetchOne(ds)
        // var users = Stmt."select * from users".fetchOne(ds, g(User.class))

        // -- FOR ???
        // void bad(@Database("replica") ds) {
        //    Stmt."select * from users".fetchOne(ds, g(User.class)
        // }

        // record DataSources(
        //    @Database("main") DataSource main,
        //    @Database("replica" ) DataSource replica
        // ){}
        // void bar(DataSources ds) {
        //     Stmt."select * from users".fetchOne(ds.main, g(User.class)
        //     Stmt."select * from users".fetchOne(ds.replica, g(User.class)
        // }
        var x = STR."""
            """;
        // Call."""
        //     """.query
        //        .update
        // Stmt."""
        //     """.queryOne(db,
        //        .queryOneOrNull
        //        .queryArray(
        //        .queryList
        //        .queryStream
        //        .update(db, flags).fetchOne(
        // Stmt.
        //    default - query, update
        //    withCursorParameters       - query -> ResultSet
        //    withGeneratedKeyParameters - update
        //
        // fetchNone
        // fetchOne
        // fetchOneOrNull
        // fetchArray
        // fetchList
        // fetchStream
        // fetch(new MyFetcher(),
        // RowsAffected
        // () -> PrepareStatement
        // var db1 = new Database1()
        // var userName = Stmt."""
        //     select * from users where id = \{userId}
        //     """.fetchOne(db, g(String.class));
        // var user = Stmt."select * from users where id = \{userId}"
        //     .<@G User>fetchOne(ds);
        //
        // Prepare
        // Execute
        // Fetch (ResultSetMappers)
        // ResultSetMapper: ResultSet -> T
        // MapRow
        // 1. Query text and parameters - Stmt / Call
        // 2. Prepare
        // 3. Execute
        // 4. Fetch(Algorithm)
        // 5. Generate row mapper: m
        // 6. Generate dto & row mapper: g
        //
        // NB: Всегда делаем stmt.execute()
        //    (affectedRows, List<ResultSet>)
        //
        // Statement -> StatementWithGeneratedKeys -> StatementWithGeneratedKeysAffectedRows
        //              StatementWithAffectedRows  ->
        // Call      -> CallWithAffectedRows
        // Batch -> BatchStatement -> BatchStatementWithGeneratedKeys -> BatchStatementWithGeneratedKeysAffectedRows
        //          BatchCall      -> BatchCallWithAffectedRows
        //
        // queryText + parameters = Statement
        // fetchOne(ds, Stmt."", m(String.class)
        // class User {
        //    long id;
        //    String name;
        //    String email;
        // }
        //
        // ResultSet -> T
        // rs -> rs.getString(1)
        // rs -> new User(rs.getLong(1), rs.getString(2). rs.getString(3))
        //
        // var userName = Stmt."""
        //     select * from users where id = \{userId}
        //     """.withGeneratedKeys().fetchOne(ds, m(User.class));
        //                            .withAffectedRows().fetchOne(ds, g(User.class))
        //                            .with(ExecutePrepareStmtXXX.class)
        // must be compiled to:
        //    withConnection(ds, conn -> {
        //        var stmt = conn.prepareStatement("text", extraArgs)
        //        stmt.setLong(1, userId)
        //        var affectedRowsCount = stmt.executeUpdate()
        //        fetchOne(stmt.getResultSet(), m(String.class)
        //        return Fetcher.fetchOne(stmt, rs -> rs.getString(1))
        //    })
        //
        //    Fetchers.fetchOne(ds, new Statement("text") {{
        //        @Override void setArgs(PreparedStatement stmt) {
        //            stmt.setLong(1, userId);
        //        }
        //    }}.withGeneratedKeys()
        //      .withAffectedRows()
        //      , rs -> rs.getString()
        //    );
        //
        // var _ = Stmt."delete from users".update(db, ps -> ps.
        //
        // val updated = new Batch(
        //     users, u -> Stmt."""
        //
        //     """
        // ).fetch(db, one(g(String.class))
        //
        // var userName = fetch(
        //     db, one(String.class), Stmt."""
        //     select * from users where id = \{userId}
        // """)
        // var _ = Call."""
        //
        //     """.fetchVoid(db)
        //
        // val updatedIds = new Batch(
        //     users, u -> Stmt."""
        //
        //     """
        // ).fetchList(ds, String.class)
        var _ = fetch(db, new PrepareStatement(), one(String.class));

        var result = TrueSql.inConnection(db, cnn -> {
            testSelect(cnn, 42L);
            var user = TrueSql.inTransaction(cnn, txn ->
                testSelect(txn, 42)
            );
            return "";
        });
    }
}
