package xyz.capybara.clamav.commands

internal object Shutdown : Command<Unit>() {
    override val commandString: String
        get() = "SHUTDOWN"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String): Unit = logger.info("Shutting down the ClamAV server")
}
