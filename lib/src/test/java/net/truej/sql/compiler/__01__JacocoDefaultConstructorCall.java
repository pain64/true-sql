package net.truej.sql.compiler;

import org.junit.jupiter.api.Test;

public class __01__JacocoDefaultConstructorCall {
    @Test public void test() throws InterruptedException {
        new TypeFinder();
        new TypeChecker();
        new GLangParser();
        new JdbcMetadataFetcher();
        new BoundTypeExtractor();
        new StatementGenerator();
        new MapperGenerator();
        new DtoGenerator();
        new InvocationsFinder();
        new InvocationsHandler();
        new ExistingDtoParser();
        new ConfigurationParser();
        new DatabaseNames();
    }
}
