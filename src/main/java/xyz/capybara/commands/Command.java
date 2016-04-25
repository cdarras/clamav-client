package xyz.capybara.commands;

import xyz.capybara.exceptions.UnknownCommandException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class Command<T> {

    public static final String UNKNOWN_COMMAND = "UNKNOWN COMMAND";

    public abstract String getCommandString();

    public T send(InetSocketAddress server) throws IOException {
        try (SocketChannel socketChannel = SocketChannel.open(server)) {
            socketChannel.write(getRawCommand());

            return readResponse(socketChannel);
        }
    }

    protected abstract CommandFormat getFormat();

    protected ByteBuffer getRawCommand() {
        StringBuilder rawCommand = new StringBuilder();
        rawCommand.append(getFormat().getPrefix())
                .append(getCommandString())
                .append(getFormat().getTerminator());

        return ByteBuffer.wrap(rawCommand.toString().getBytes(StandardCharsets.US_ASCII));
    }

    protected T readResponse(SocketChannel socketChannel) throws IOException {
        StringBuilder responseStringBuilder = new StringBuilder();
        ByteBuffer rawResponsePart = ByteBuffer.allocate(32);

        for (int read = socketChannel.read(rawResponsePart); read > -1; read = socketChannel.read(rawResponsePart)) {
            responseStringBuilder.append(new String(rawResponsePart.array(), StandardCharsets.US_ASCII));
            rawResponsePart = ByteBuffer.allocate(32);
        }

        String responseString = normalizeResponseString(responseStringBuilder.toString());

        if (responseString.equals(UNKNOWN_COMMAND)) {
            throw new UnknownCommandException(getCommandString());
        }

        log.debug("{} - Response: {}", getCommandString(), responseString);

        return parseResponse(responseString);
    }

    private String normalizeResponseString(String responseString) {
        responseString = responseString.replaceAll("\0+", "");
        if (getFormat() == CommandFormat.NEW_LINE) {
            responseString = responseString.substring(0, responseString.lastIndexOf('\n'));
        }

        return responseString;
    }

    protected abstract T parseResponse(String responseString);

    protected enum CommandFormat {
        NULL_CHAR('z', '\0'),
        NEW_LINE('n', '\n');

        @Getter
        private char prefix, terminator;

        CommandFormat(char prefix, char terminator) {
            this.prefix = prefix;
            this.terminator = terminator;
        }
    }
}