package com.truej.sql.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.truej.sql.util.CompileAssert.*;

public class Test1 {
    @Test void first() throws IOException {
        assertGenerated(
            """
                package truej;
                import com.truej.sql.v3.TrueSql;
                
                @TrueSql.Process class Simple {
                  void simple() {
                    
                  }
                }"""
        );

    }
}
