package xyz.capybara.clamav.commands;

public class Stats extends Command<String> {

    public static final String COMMAND = "STATS";

    @Override
    public String getCommandString() {
        return COMMAND;
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NEW_LINE;
    }

    @Override
    protected String parseResponse(String responseString) {
        return responseString;
    }
}
