package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class __02__ConfigurationParser {
    @Test public void test() throws InterruptedException {
        var pName = "__magic__truesql__env__config";
        System.setProperty(pName, "xxx");
        Assertions.assertEquals("xxx", ConfigurationParser.findProperty(Map.of(), pName));

        var env = System.getenv().entrySet().stream().toList().getFirst();

        Assertions.assertEquals(
            env.getValue(), ConfigurationParser.findProperty(Map.of(), env.getKey())
        );
    }
}
