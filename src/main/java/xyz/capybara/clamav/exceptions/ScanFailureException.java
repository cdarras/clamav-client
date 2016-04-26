package xyz.capybara.clamav.exceptions;

public class ScanFailureException extends RuntimeException {

    public ScanFailureException(String responseString) {
        super(responseString);
    }
}
