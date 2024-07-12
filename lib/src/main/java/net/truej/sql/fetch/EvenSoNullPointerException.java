package net.truej.sql.fetch;

public class EvenSoNullPointerException extends RuntimeException {

    public static <T> T check(T object) {
        if (object == null)
            throw new EvenSoNullPointerException();
        return object;
    }
}
