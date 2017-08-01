package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object Ping : Command<Unit>() {
    override val commandString: String
        get() = "PING"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String): Unit {
        if (responseString != "PONG") {
            throw InvalidResponseException(responseString)
        }
        logger.debug(responseString)
    }
}
