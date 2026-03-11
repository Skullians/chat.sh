package net.skullian.zenith.core.flavor.exception;

public class FlavorException extends RuntimeException {
    public FlavorException(String message) {
        super(message);
    }

    public FlavorException(String message, Throwable cause) {
        super(message, cause);
    }
}
