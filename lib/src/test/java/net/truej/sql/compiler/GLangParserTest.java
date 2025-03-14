
package net.truej.sql.compiler;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.truej.sql.compiler.GLangParser.*;

public class GLangParserTest {
    void assertLex(String input, Lexeme... lexemes) {
        Assertions.assertEquals(Arrays.asList(lexemes), lex(input));
    }

    @Test void lexTest() {
        assertLex("", new End());
        assertLex("a", new Text("a"), new End());
        assertLex("\\a\\\\", new Text("\\a\\\\"), new End());

        assertLex("aa.b.", new Text("aa"), new Dot(), new Text("b"), new Dot(), new End());
        assertLex("a\\.b", new Text("a.b"), new End());
        assertLex("a.  b", new Text("a"), new Dot(), new Text("b"), new End());

        assertLex("aa:b:", new Text("aa"), new Colon(), new Text("b"), new Colon(), new End());
        assertLex("a\\:b", new Text("a:b"), new End());

        assertLex("aa b ", new Text("aa"), new Text("b"), new End());
        assertLex(" aa  b  ", new Text("aa"), new Text("b"), new End());
        assertLex("a\\ \\ b", new Text("a  b"), new End());
        assertLex("a\\  \\ b", new Text("a "), new Text(" b"), new End());
    }
}
