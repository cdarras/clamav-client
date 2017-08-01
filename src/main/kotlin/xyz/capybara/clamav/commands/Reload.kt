package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object Reload : Command<Unit>() {
    override val commandString: String
        get() = "RELOAD"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String): Unit {
        if (responseString != "RELOADING") {
            throw InvalidResponseException(responseString)
        }
        logger.info("Reloading the virus databases")
    }
}
