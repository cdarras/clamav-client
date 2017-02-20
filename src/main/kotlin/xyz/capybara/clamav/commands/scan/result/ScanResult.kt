package xyz.capybara.clamav.commands.scan.result

/**
 * This class holds the result of an antivirus scan.
 * It contains a status and a map filled as following:
 *
 *  * Key: infected file path
 *  * Value: list of viruses found in the file
 *
 */
data class ScanResult(val status: Status, val foundViruses: Map<String, Collection<String>> = emptyMap()) {
    enum class Status {
        OK, VIRUS_FOUND
    }
}
