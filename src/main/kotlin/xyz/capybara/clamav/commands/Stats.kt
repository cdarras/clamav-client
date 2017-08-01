package xyz.capybara.clamav.commands

internal object Stats : Command<String>() {
    override val commandString: String
        get() = "STATS"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NEW_LINE

    override fun parseResponse(responseString: String): String = responseString
}
