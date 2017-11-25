package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object VersionCommands : Command<Collection<String>>() {
    private const val COMMANDS_START_TAG = "| COMMANDS:"

    override val commandString
        get() = "VERSIONCOMMANDS"

    override val format
        get() = Command.CommandFormat.NEW_LINE

    override fun parseResponse(responseString: String): Collection<String> {
        val commandsStartPos = responseString.indexOf(COMMANDS_START_TAG)
        return when (commandsStartPos) {
            -1 -> throw InvalidResponseException(responseString)
            else -> responseString.substring(commandsStartPos + COMMANDS_START_TAG.length).split(" ".toRegex())
        }
    }
}
