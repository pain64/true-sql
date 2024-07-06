package net.truej.sql.showcase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.truej.sql.source.Parameters.Nullable;

public class __13__NullSafety {
    // in query check mode:
    //     TrueSql forces you to annotate field as @Nullable
    record User(long id, @Nullable String name) { }

    // в некоторых случаях драйвер неправильно выводит nullability колонок
    // тогда вы можете "сказать что вы правы" и получите warning

    // force that field is NotNull: override drive decision
    record Clinic(long id, @NotNull String name) { }
    // if your decision and driver decision is different - YOU HAVE COMPTIME WARNING, and TICKET
    // in db vendor bug tracker

    void inDto(MainDataSource ds) {
        ds.q("select id, name from users")
            .fetchList(User.class);
    }

    record UserG(long id, @Nullable String name) { }

    void inDtoG(MainDataSource ds) {
        ds.q("""
            select id, name as ":t!" from users"""
        ).fetchList(UserG.class);
    }

    void inScalar(MainDataSource ds) {
        ds.q("select name from users")
            .fetchList(Nullable, User.class);
    }
}
