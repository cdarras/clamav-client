package xyz.capybara.clamav.commands;

import xyz.capybara.clamav.exceptions.InvalidResponseException;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class VersionCommands extends Command<Collection<String>> {

    public static final String COMMAND = "VERSIONCOMMANDS";
    private static final String COMMANDS_START_TAG = "| COMMANDS:";

    @Override
    public String getCommandString() {
        return COMMAND;
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NEW_LINE;
    }

    @Override
    protected Collection<String> parseResponse(String responseString) {
        int commandsStartPos = responseString.indexOf(COMMANDS_START_TAG);

        if (commandsStartPos == -1) {
            throw new InvalidResponseException(responseString);
        }

        return Arrays.stream(responseString.substring(commandsStartPos + COMMANDS_START_TAG.length()).split(" "))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
