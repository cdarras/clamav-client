package xyz.capybara.exceptions;

public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String responseString) {
        super(responseString);
    }
}
