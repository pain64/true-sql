package net.truej.sql.fetch;

class MissConfigurationException extends RuntimeException {
    static boolean isJacocoWorkaroundEnabled = false;

    MissConfigurationException(String message) { super(message); }

    static <T> T raise() {
        if (isJacocoWorkaroundEnabled) return null;

        throw new MissConfigurationException(
            "TrueSql compiler plugin not enabled. Check out your build tool configuration" +
            " (Gradle, Maven, ...)"
        );
    }
}
