package xyz.capybara.commands;

import xyz.capybara.exceptions.InvalidResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VersionCommands extends Command<List<String>> {

    private static final String COMMANDS_START_TAG = "| COMMANDS:";

    @Override
    public String getCommandString() {
        return "VERSIONCOMMANDS";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NEW_LINE;
    }

    @Override
    protected List<String> parseResponse(String responseString) {
        int commandsStartPos = responseString.indexOf(COMMANDS_START_TAG);

        if (commandsStartPos == -1) {
            throw new InvalidResponseException(responseString);
        }

        return Arrays.stream(responseString.substring(commandsStartPos + COMMANDS_START_TAG.length()).split(" "))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
