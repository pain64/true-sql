package net.truej.sql.showcase;

public class ProcessorTest {
    interface Action<T> {
        T act(B b);
    }

    static <T> T with(Action<T> action) {
        return null;
    }

    class B implements StringTemplate.Processor<String, RuntimeException> {
        @Override public String process(StringTemplate stringTemplate) throws RuntimeException {
            return "";
        }
    }

    void test() {
//        var x1 = with(b -> {
//            // ProcessorTest.java:25: error: processor type cannot be a raw type: <any>
//            return b."aaa";
//        });

        // workaround
        var x2 = with((B b) -> {
            // ProcessorTest.java:25: error: processor type cannot be a raw type: <any>
            return b."aaa";
        });
    }
}
