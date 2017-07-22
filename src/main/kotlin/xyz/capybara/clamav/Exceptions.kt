package xyz.capybara.clamav

class ClamavException(cause: Throwable) : RuntimeException(cause)

class CommunicationException(cause: Throwable) : RuntimeException("Error while communicating with the server", cause)

class InvalidResponseException(responseString: String) : RuntimeException(String.format("Invalid response: %s", responseString))

class ScanFailureException(responseString: String) : RuntimeException(String.format("Scan failure: %s", responseString))

class UnknownCommandException(command: String) : RuntimeException(String.format("Unknown command: %s", command))

class UnsupportedCommandException(command: String) : RuntimeException(String.format("Unsupported command: %s", command))