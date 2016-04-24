package xyz.capybara.commands.scan;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MultiScan extends ScanCommand {

    private String path;

    public MultiScan(String path) {
        this.path = path;
    }

    @Override
    public String getCommandString() {
        return "MULTISCAN";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NEW_LINE;
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
