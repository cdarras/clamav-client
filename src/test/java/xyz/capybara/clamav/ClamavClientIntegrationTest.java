package xyz.capybara.clamav;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.capybara.clamav.commands.*;
import xyz.capybara.clamav.commands.scan.ContScan;
import xyz.capybara.clamav.commands.scan.InStream;
import xyz.capybara.clamav.commands.scan.MultiScan;
import xyz.capybara.clamav.commands.scan.Scan;
import xyz.capybara.clamav.exceptions.ClamavException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.assertj.core.api.BDDAssertions.then;
import static xyz.capybara.clamav.commands.Command.CommandFormat;

@Slf4j
public class ClamavClientIntegrationTest {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3310;

    private TestServer testServerThread;

    @Before
    public void setUp() {
        testServerThread = new TestServer();
        testServerThread.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        testServerThread.interrupt();
    }

    @Test
    public void should_ping() {
        // Given
        ClamavException exception = null;
        ClamavClient client = new ClamavClient(SERVER_HOST, SERVER_PORT);
        // When
        try {
            client.ping();
        } catch (ClamavException e) {
            exception = e;
        }
        // Then
        then(exception).isNull();
    }

    private class TestServer extends Thread {
        @Override
        public void run() {
            log.debug("Starting Test Server...");

            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                serverChannel.socket().bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

                while (true) {
                    SocketChannel clientChannel = null;
                    try {
                        try {
                            clientChannel = serverChannel.accept();
                        } catch (ClosedByInterruptException e) {
                            log.debug("Test Server received an interruption signal");
                            return;
                        }
                        Request request = readRequest(clientChannel);
                        String requestCommand = getCommandFromRequest(request);
                        log.debug("Request command: {}", requestCommand);

                        switch (requestCommand) {
                            case Ping.COMMAND:
                                pingResponse(request);
                                break;
                            case Stats.COMMAND:
                                statsResponse(request);
                                break;
                            case Version.COMMAND:
                                versionResponse(request);
                                break;
                            case VersionCommands.COMMAND:
                                versionCommandsResponse(request);
                                break;
                            case Scan.COMMAND:
                            case ContScan.COMMAND:
                            case MultiScan.COMMAND:
                                scanResponse(request);
                                break;
                            case Reload.COMMAND:
                                reloadResponse(request);
                                break;
                            case Shutdown.COMMAND:
                                return;
                            default:
                                unknownCommandResponse(request);
                                return;
                        }
                    } finally {
                        if (clientChannel != null && clientChannel.isOpen()) {
                            clientChannel.close();
                        }
                    }
                }
            } catch (IOException e) {
                log.error("IOException in Test Server: {}", e.getMessage());
            } finally {
                log.debug("Shutting down Test Server...");
            }
        }

        private Request readRequest(SocketChannel socketChannel) throws IOException {
            CommandFormat format = readCommandFormat(socketChannel);
            StringBuilder requestStringBuilder = new StringBuilder();
            ByteBuffer rawRequestPart = ByteBuffer.allocate(32);

            for (int read = socketChannel.read(rawRequestPart); read > -1; read = socketChannel.read(rawRequestPart)) {
                String rawRequestPartString = new String(rawRequestPart.array(), StandardCharsets.US_ASCII);
                rawRequestPartString = rawRequestPartString.substring(0, read);
                requestStringBuilder.append(rawRequestPartString);

                if (rawRequestPartString.endsWith(String.valueOf(format.getTerminator()))) {
                    break;
                }

                rawRequestPart = ByteBuffer.allocate(32);
            }

            return new Request(socketChannel, format, removeRequestTerminator(requestStringBuilder.toString(), format));
        }

        private CommandFormat readCommandFormat(SocketChannel socketChannel) throws IOException {
            ByteBuffer rawPrefix = ByteBuffer.allocate(1);
            socketChannel.read(rawPrefix);
            char prefix = new String(rawPrefix.array(), StandardCharsets.US_ASCII).toCharArray()[0];

            return CommandFormat.fromPrefix(prefix);
        }

        private String removeRequestTerminator(String requestString, CommandFormat format) {
            return requestString.substring(0, requestString.lastIndexOf(format.getTerminator()));
        }

        private String getCommandFromRequest(Request request) {
            String content = request.getContent();
            return content.split(" ")[0];
        }

        private void writeResponse(SocketChannel socketChannel,
                                   CommandFormat format,
                                   String response) throws IOException {
            byte[] rawResponse = response.getBytes(StandardCharsets.US_ASCII);
            ByteBuffer buffer = ByteBuffer.wrap(rawResponse);
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            ByteBuffer terminator = ByteBuffer.wrap(String.valueOf(format.getTerminator())
                    .getBytes(StandardCharsets.US_ASCII));
            while (terminator.hasRemaining()) {
                socketChannel.write(terminator);
            }
        }

        private void pingResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(), request.getFormat(), "PONG");
        }

        private void statsResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(), request.getFormat(), "Some stats here");
        }

        private void versionResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(), request.getFormat(), "ClamAV Test Server");
        }

        private void versionCommandsResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(),
                    request.getFormat(),
                    "ClamAV Test Server | COMMANDS: " +
                            "PING STATS VERSION VERSIONCOMMANDS SCAN CONTSCAN MULTISCAN RELOAD SHUTDOWN");
        }

        private void scanResponse(Request request) throws IOException {
            StringBuilder response = new StringBuilder();
            response.append(request.getContent().split(" ")[1])
                    .append(' ')
                    .append("OK");
            writeResponse(request.getSocketChannel(), request.getFormat(), response.toString());
        }

        private void reloadResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(), request.getFormat(), "RELOADING");
        }

        private void unknownCommandResponse(Request request) throws IOException {
            writeResponse(request.getSocketChannel(), request.getFormat(), "UNKNOWN COMMAND");
        }

        @AllArgsConstructor
        @Getter
        private class Request {
            private SocketChannel socketChannel;
            private CommandFormat format;
            private String content;
        }
    }
}
