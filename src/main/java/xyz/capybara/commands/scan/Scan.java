package xyz.capybara.commands.scan;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Scan extends ScanCommand {

    private String path;

    public Scan(String path) {
        this.path = path;
    }

    @Override
    public String getCommandString() {
        return "SCAN";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    protected ByteBuffer getRawCommand() {
        StringBuilder rawCommand = new StringBuilder();
        rawCommand.append(getFormat().getPrefix())
                .append(getCommandString())
                .append(' ')
                .append(path)
                .append(getFormat().getTerminator());

        return ByteBuffer.wrap(rawCommand.toString().getBytes(StandardCharsets.US_ASCII));
    }
}
