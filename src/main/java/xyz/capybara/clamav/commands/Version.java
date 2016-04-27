package xyz.capybara.clamav.commands;

public class Version extends Command<String> {

    public static final String COMMAND = "VERSION";

    @Override
    public String getCommandString() {
        return COMMAND;
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    protected String parseResponse(String responseString) {
        return responseString;
    }
}
