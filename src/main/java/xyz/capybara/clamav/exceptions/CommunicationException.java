package xyz.capybara.clamav.exceptions;

public class CommunicationException extends RuntimeException {

    public CommunicationException(Throwable cause) {
        super("Error while communicating with the server", cause);
    }
}
