package xyz.capybara.clamav.exceptions;

public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String responseString) {
        super(String.format("Invalid response: %", responseString));
    }
}
