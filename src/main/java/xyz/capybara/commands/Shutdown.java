package xyz.capybara.commands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shutdown extends Command<Void> {

    @Override
    public String getCommandString() {
        return "SHUTDOWN";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    protected Void parseResponse(String responseString) {
        log.info("Shutting down the ClamAV server");
        // no response
        return null;
    }
}
