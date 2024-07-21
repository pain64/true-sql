package net.truej.sql.dsl;

// FIXME: make this class package-private
public class MissConfigurationException extends RuntimeException {
    // TODO: move Parameters to this package

    MissConfigurationException(String message) { super(message); }

    // FIXME: make this method package-private
    public static <T> T raise() {
        throw new MissConfigurationException(
            "TrueSql compiler plugin not enabled. Check out your build tool configuration" +
            " (Gradle, Maven, ...)"
        );
    }
}
