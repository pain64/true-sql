package net.truej.sql.fetch;

class MissConfigurationException extends RuntimeException {

    MissConfigurationException(String message) { super(message); }

    static <T> T raise() {
        throw new MissConfigurationException(
            "TrueSql compiler plugin not enabled. Check out your build tool configuration" +
            " (Gradle, Maven, ...)"
        );
    }
}
