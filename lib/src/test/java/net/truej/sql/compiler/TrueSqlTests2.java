package net.truej.sql.compiler;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.List;
import java.util.stream.Stream;

public class TrueSqlTests2 implements TestTemplateInvocationContextProvider {
    @Override public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        return Stream.empty();
    }

    private TestTemplateInvocationContext invocationContext(final String database) {
        return new TestTemplateInvocationContext() {  

            @Override
            public String getDisplayName(int invocationIndex) {
                return database;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
//                final JdbcDatabaseContainer databaseContainer = containers.get(database);
//                return asList(
//                    (BeforeEachCallback) context -> databaseContainer.start(),
//                    (AfterAllCallback)   context -> databaseContainer.stop(),
//                    new ParameterResolver() {
//
//                        @Override
//                        public boolean supportsParameter(ParameterContext parameterCtx, ExtensionContext extensionCtx) {
//                            return parameterCtx.getParameter().getType().equals(JdbcDatabaseContainer.class);
//                        }
//
//                        @Override
//                        public Object resolveParameter(ParameterContext parameterCtx, ExtensionContext extensionCtx) {
//                            return databaseContainer;
//                        }
//                    });
                return null;
            }
        };
    }
}
