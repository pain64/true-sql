package net.truej.sql.fetch;

import net.truej.sql.bindings.Standard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;

import static net.truej.sql.fetch.Parameters.*;

public class __25__TrueSqlMissConfiguration {
    class A implements As { }
    class B implements NoUpdateCount, NoUpdateCount.None, NoUpdateCount.OneG,
        NoUpdateCount.One, NoUpdateCount.OneOrZero, NoUpdateCount.OneOrZeroG, NoUpdateCount.ListG,
        NoUpdateCount.List_, NoUpdateCount.StreamG, NoUpdateCount.Stream_ { }
    class C implements UpdateCount, UpdateCount.None, UpdateCount.OneG,
        UpdateCount.One, UpdateCount.OneOrZero, UpdateCount.OneOrZeroG, UpdateCount.ListG,
        UpdateCount.List_, UpdateCount.StreamG, UpdateCount.Stream_ { }


    @Test void test() throws Throwable {
        // for Jacoco
        new Parameters();
        new Standard();

        var actions = List.<Executable>of(
            () -> out(Integer.class),
            () -> inout(1),
            () -> unfold(new ArrayList<>()),
            () -> unfold(new ArrayList<Integer>(), (x) -> new Object[]{x}),
            () -> inout(1),
            () -> new Q<>() { }.q("hello"),
            () -> new Q<>() { }.q(List.of(), "hello", v -> new Object[]{}),
            () -> new A().asCall(),
            () -> new A().asGeneratedKeys("id"),
            () -> new A().asGeneratedKeys(1),
            () -> new B().fetchNone(),
            () -> new B().fetchOne(String.class),
            () -> new B().fetchOne(String.class),
            () -> new B().fetchOne(Nullable, String.class),
            () -> new B().fetchOne(NotNull, String.class),
            () -> new B().fetchOneOrZero(String.class),
            () -> new B().fetchOneOrZero(NotNull, String.class),
            () -> new B().fetchOneOrZero(Nullable, String.class),
            () -> new B().fetchList(String.class),
            () -> new B().fetchList(NotNull, String.class),
            () -> new B().fetchList(Nullable, String.class),
            () -> new B().fetchStream(String.class),
            () -> new B().fetchStream(NotNull, String.class),
            () -> new B().fetchStream(Nullable, String.class),
            () -> new C().fetchNone(),
            () -> new C().fetchOne(String.class),
            () -> new C().fetchOne(Nullable, String.class),
            () -> new C().fetchOne(NotNull, String.class),
            () -> new C().fetchOneOrZero(String.class),
            () -> new C().fetchOneOrZero(NotNull, String.class),
            () -> new C().fetchOneOrZero(Nullable, String.class),
            () -> new C().fetchList(String.class),
            () -> new C().fetchList(NotNull, String.class),
            () -> new C().fetchList(Nullable, String.class),
            () -> new C().fetchStream(String.class),
            () -> new C().fetchStream(NotNull, String.class),
            () -> new C().fetchStream(Nullable, String.class)
        );

        for (var action : actions)
            Assertions.assertThrows(MissConfigurationException.class, action);

        MissConfigurationException.isJacocoWorkaroundEnabled = true;
        for (var action : actions)
            action.execute();
    }
}
