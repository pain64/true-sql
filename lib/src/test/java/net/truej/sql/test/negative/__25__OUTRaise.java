package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.dsl.MissConfigurationException;
import net.truej.sql.dsl.Q;
import net.truej.sql.source.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.source.Parameters.inout;
import static net.truej.sql.source.Parameters.out;

//@TrueSqlTests2.Message(
//    kind = ERROR, text = "TrueSql compiler plugin not enabled. Check out your build tool configuration (Gradle, Maven, ...)"
//)
public class __25__OUTRaise {
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

    }
}
