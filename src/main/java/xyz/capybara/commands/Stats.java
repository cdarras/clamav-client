package xyz.capybara.commands;

public class Stats extends Command<String> {

    @Override
    public String getCommandString() {
        return "STATS";
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
