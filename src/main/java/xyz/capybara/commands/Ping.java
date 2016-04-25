package xyz.capybara.commands;

import xyz.capybara.exceptions.InvalidResponseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ping extends Command<Void> {

    @Override
    public String getCommandString() {
        return "PING";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    protected Void parseResponse(String responseString) {
        if (!responseString.equals("PONG")) {
            throw new InvalidResponseException(responseString);
        }

        log.info(responseString);

        return null;
    }
}
