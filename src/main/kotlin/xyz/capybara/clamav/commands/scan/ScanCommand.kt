package xyz.capybara.clamav.commands.scan

import xyz.capybara.clamav.InvalidResponseException
import xyz.capybara.clamav.ScanFailureException
import xyz.capybara.clamav.commands.Command
import xyz.capybara.clamav.commands.scan.result.ScanResult

internal abstract class ScanCommand : Command<ScanResult>() {

    override fun parseResponse(responseString: String): ScanResult {
        try {
            return when {
                RESPONSE_OK matches responseString -> ScanResult(ScanResult.Status.OK)
                RESPONSE_VIRUS_FOUND.containsMatchIn(responseString) -> {
                    // add every found viruses to the scan result, grouped by infected file
                    val foundViruses = responseString.split("\n".toRegex())
                            .mapNotNull(RESPONSE_VIRUS_FOUND_LINE::matchEntire)
                            .map { it.groups.reversed() }
                            // key: file path
                            // value: virus name
                            .groupBy({ it[1]!!.value }, { it[0]!!.value })
                    ScanResult(ScanResult.Status.VIRUS_FOUND, foundViruses)
                }
                RESPONSE_ERROR matches responseString -> throw ScanFailureException(responseString)
                else -> throw InvalidResponseException(responseString)
            }
        } catch (e: IllegalStateException) {
            throw InvalidResponseException(responseString)
        }
    }

    companion object {
        private val RESPONSE_OK = Regex(
                "(.+) OK$",
                RegexOption.UNIX_LINES
        )
        private val RESPONSE_VIRUS_FOUND = Regex(
                "(.+) FOUND$",
                setOf(RegexOption.MULTILINE, RegexOption.UNIX_LINES)
        )
        private val RESPONSE_ERROR = Regex(
                "(.+) ERROR",
                RegexOption.UNIX_LINES
        )
        private val RESPONSE_VIRUS_FOUND_LINE = Regex(
                "(.+: )?(.+): (.+) FOUND$",
                RegexOption.UNIX_LINES
        )
    }
}