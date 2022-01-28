package xyz.capybara.clamav.commands

import xyz.capybara.clamav.InvalidResponseException

internal object VersionCommands : Command<Collection<String>>() {
    private const val COMMANDS_START_TAG = "| COMMANDS:"

    override val commandString
        get() = "VERSIONCOMMANDS"

    override val format
        get() = CommandFormat.NEW_LINE

    override fun parseResponse(responseString: String): Collection<String> {
        return when (val commandsStartPos = responseString.indexOf(COMMANDS_START_TAG)) {
            -1 -> throw InvalidResponseException(responseString)
            else -> responseString.substring(commandsStartPos + COMMANDS_START_TAG.length).split(" ".toRegex())
        }
    }
}
