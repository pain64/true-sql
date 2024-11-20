package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __15__AnotherLibraryCallRecognizedAsTrueSql {
    static class Confusion {
        Confusion q(String text) { return this; }
        Void fetchNone() { return null; }
    }
    // TrueSql recognizes that it's not genuine TrueSql api call!
    // Errors discovered for this tree in annotation processor
    // will be discarded in compiler plugin
    @TestTemplate void test() throws SQLException {
        var cn = new Confusion();
        cn.q("").fetchNone();
    }
}
