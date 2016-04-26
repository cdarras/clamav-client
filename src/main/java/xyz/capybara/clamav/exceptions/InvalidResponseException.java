package xyz.capybara.clamav.exceptions;

public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String responseString) {
        super(responseString);
    }
}
