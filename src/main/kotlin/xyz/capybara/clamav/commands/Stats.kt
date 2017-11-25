package xyz.capybara.clamav.commands

internal object Stats : Command<String>() {
    override val commandString
        get() = "STATS"

    override val format
        get() = Command.CommandFormat.NEW_LINE

    override fun parseResponse(responseString: String) = responseString
}
