package net.truej.sql.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.truej.sql.fetch.Parameters.*;

public class __25__TrueSqlMissConfiguration {
    class A implements As {}
    class B implements NoUpdateCount, NoUpdateCount.None, NoUpdateCount.OneG,
        NoUpdateCount.One, NoUpdateCount.OneOrZero, NoUpdateCount.OneOrZeroG, NoUpdateCount.ListG,
        NoUpdateCount.List_, NoUpdateCount.StreamG, NoUpdateCount.Stream_ {}
    class C implements UpdateCount, UpdateCount.None, UpdateCount.OneG,
        UpdateCount.One, UpdateCount.OneOrZero, UpdateCount.OneOrZeroG, UpdateCount.ListG,
        UpdateCount.List_, UpdateCount.StreamG, UpdateCount.Stream_ {}
    @Test
    void test() {
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> out(Integer.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> inout(1)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new Q<>() {}.q("hello")
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new Q<>() {}.q(List.of(), "hello", v -> new Object[]{})
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new A().asCall()
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new A().asGeneratedKeys("id")
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new A().asGeneratedKeys(1)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchNone()
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOne(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOne(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOne(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOne(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOneOrZero(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOneOrZero(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchOneOrZero(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchList(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchList(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchList(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchStream(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchStream(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new B().fetchStream(Nullable, String.class)
        );

        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchNone()
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOne(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOne(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOne(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOne(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOneOrZero(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOneOrZero(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchOneOrZero(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchList(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchList(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchList(Nullable, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchStream(String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchStream(NotNull, String.class)
        );
        Assertions.assertThrows(
            MissConfigurationException.class,
            () -> new C().fetchStream(Nullable, String.class)
        );

    }
}
