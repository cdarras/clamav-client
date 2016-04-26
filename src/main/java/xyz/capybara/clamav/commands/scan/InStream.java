package xyz.capybara.clamav.commands.scan;

import xyz.capybara.clamav.commands.Command;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.exceptions.CommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class InStream extends ScanCommand {

    private static final int CHUNK_SIZE = 2048;

    private InputStream inputStream;

    public InStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String getCommandString() {
        return "INSTREAM";
    }

    @Override
    protected CommandFormat getFormat() {
        return CommandFormat.NULL_CHAR;
    }

    @Override
    public ScanResult send(InetSocketAddress server) {
        try (SocketChannel socketChannel = SocketChannel.open(server)) {
            socketChannel.write(getRawCommand());

            // ByteBuffer order must be big-endian ( == network byte order)
            // It is, by default, but it doesn't hurt to set it anyway
            ByteBuffer length = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            byte[] data = new byte[CHUNK_SIZE];
            int chunkSize = CHUNK_SIZE;
            while (chunkSize == CHUNK_SIZE) {
                chunkSize = inputStream.read(data);
                if (chunkSize > 0) {
                    length.clear();
                    length.putInt(chunkSize).flip();
                    // The format of the chunk is: '<length><data>'
                    socketChannel.write(length);
                    socketChannel.write(ByteBuffer.wrap(data, 0, chunkSize));
                }
            }
            length.clear();
            // Terminate the stream by sending a zero-length chunk
            length.putInt(0).flip();
            socketChannel.write(length);

            return readResponse(socketChannel);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }
}
