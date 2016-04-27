package xyz.capybara.clamav.exceptions;

public class ScanFailureException extends RuntimeException {

    public ScanFailureException(String responseString) {
        super(String.format("Scan failure: %s", responseString));
    }
}
