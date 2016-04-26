package xyz.capybara.clamav.commands;

import xyz.capybara.clamav.exceptions.InvalidResponseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reload extends Command<Void> {

    @Override
    public String getCommandString() {
        return "RELOAD";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    protected Void parseResponse(String responseString) {
        if (!responseString.equals("RELOADING")) {
            throw new InvalidResponseException(responseString);
        }

        log.info("Reloading the virus databases");

        return null;
    }
}
