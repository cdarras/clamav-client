package xyz.capybara.clamav.commands

internal object Shutdown : Command<Unit>() {
    override val commandString
        get() = "SHUTDOWN"

    override val format
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String) = logger.info { "Shutting down the ClamAV server" }
}
