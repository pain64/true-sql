package net.truej.sql.compiler;

import net.truej.sql.test.DTO;
import org.junit.jupiter.api.Test;


public class __04__DoofyEncoder {
    @Test public void test() {
        var dto = new DTO(5);
        TrueSqlPlugin.doofyEncode(dto);
    }
}
