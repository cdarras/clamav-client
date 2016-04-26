package xyz.capybara.clamav.exceptions;

public class UnsupportedCommandException extends RuntimeException {

    public UnsupportedCommandException(String command) {
        super(String.format("The %s command is unsupported", command));
    }
}
