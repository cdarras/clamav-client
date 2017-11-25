package xyz.capybara.clamav.commands

internal object Version : Command<String>() {
    override val commandString
        get() = "VERSION"

    override val format
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String) = responseString
}
