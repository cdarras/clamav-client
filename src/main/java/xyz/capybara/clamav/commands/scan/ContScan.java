package xyz.capybara.clamav.commands.scan;

import xyz.capybara.clamav.commands.Command;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ContScan extends ScanCommand {

    private String path;

    public ContScan(String path) {
        this.path = path;
    }

    @Override
    protected Command.CommandFormat getFormat() {
        return Command.CommandFormat.NEW_LINE;
    }

    @Override
    public String getCommandString() {
        return "CONTSCAN";
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
