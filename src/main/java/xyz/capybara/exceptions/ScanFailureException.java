package xyz.capybara.exceptions;

public class ScanFailureException extends RuntimeException {

    public ScanFailureException(String responseString) {
        super(responseString);
    }
}
