package xyz.capybara.commands;

public class Version extends Command<String> {

    @Override
    public String getCommandString() {
        return "VERSION";
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
