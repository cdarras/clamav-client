package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object Ping : Command<Unit>() {
    override val commandString
        get() = "PING"

    override val format
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String) {
        if (responseString != "PONG") {
            throw InvalidResponseException(responseString)
        }
        logger.debug(responseString)
    }
}
