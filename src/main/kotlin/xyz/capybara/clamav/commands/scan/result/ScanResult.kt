package xyz.capybara.clamav.commands.scan.result

/**
 * This class holds the result of an antivirus scan.
 * It contains a status and a map filled as following:
 *
 *  * Key: infected file path
 *  * Value: list of viruses found in the file
 *
 */
sealed class ScanResult {
    object OK : ScanResult()
    data class VirusFound(val foundViruses: Map<String, Collection<String>> = emptyMap()) : ScanResult()
}