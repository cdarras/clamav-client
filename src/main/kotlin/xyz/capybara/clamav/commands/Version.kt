package xyz.capybara.clamav.commands

internal object Version : Command<String>() {
    override val commandString: String
        get() = "VERSION"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NULL_CHAR

    override fun parseResponse(responseString: String): String {
        return responseString
    }
}
