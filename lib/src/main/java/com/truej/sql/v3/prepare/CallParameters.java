package com.truej.sql.v3.prepare;

public class CallParameters {
    public static Void out(String parameterName) {
        return null;
    }

    public static <T> T inout(String parameterName, T value) {
        return value;
    }
}
