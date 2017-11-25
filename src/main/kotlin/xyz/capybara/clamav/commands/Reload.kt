package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object Reload : Command<Unit>() {
    override val commandString
        get() = "RELOAD"

    override val format
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String) {
        if (responseString != "RELOADING") {
            throw InvalidResponseException(responseString)
        }
        logger.info { "Reloading the virus databases" }
    }
}
