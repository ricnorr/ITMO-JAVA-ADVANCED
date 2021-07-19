package info.kgeorgiy.ja.korobejnikov.walk;

public class WalkException extends RuntimeException {
    public WalkException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalkException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
