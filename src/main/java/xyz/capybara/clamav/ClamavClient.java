package xyz.capybara.clamav;

import xyz.capybara.clamav.commands.Stats;
import xyz.capybara.clamav.commands.Version;
import xyz.capybara.clamav.commands.scan.InStream;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.Command;
import xyz.capybara.clamav.commands.Ping;
import xyz.capybara.clamav.commands.Reload;
import xyz.capybara.clamav.commands.Shutdown;
import xyz.capybara.clamav.commands.VersionCommands;
import xyz.capybara.clamav.commands.scan.ContScan;
import xyz.capybara.clamav.commands.scan.MultiScan;
import xyz.capybara.clamav.commands.scan.Scan;
import xyz.capybara.clamav.configuration.Platform;
import xyz.capybara.clamav.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;

public class ClamavClient {
    public static final int DEFAULT_SERVER_PORT = 3310;
    public static final Platform DEFAULT_SERVER_PLATFORM = Platform.JVM_PLATFORM;

    private InetSocketAddress server;
    private Platform serverPlatform;
    private List<String> availableCommands;

    public ClamavClient(String serverHostname) {
        this(serverHostname, DEFAULT_SERVER_PORT);
    }

    public ClamavClient(String serverHostname, int serverPort) {
        this(serverHostname, serverPort, DEFAULT_SERVER_PLATFORM);
    }

    public ClamavClient(String serverHostname, Platform serverPlatform) {
        this(serverHostname, DEFAULT_SERVER_PORT, serverPlatform);
    }

    public ClamavClient(String serverHostname, int serverPort, Platform serverPlatform) {
        this(new InetSocketAddress(serverHostname, serverPort), serverPlatform);
    }

    public ClamavClient(InetSocketAddress server) {
        this(server, DEFAULT_SERVER_PLATFORM);
    }

    public ClamavClient(InetSocketAddress server, Platform serverPlatform) {
        this.server = server;
        this.serverPlatform = serverPlatform;
    }

    public void ping() {
        sendCommand(new Ping());
    }

    public String version() {
        return sendCommand(new Version());
    }

    public String stats() {
        return sendCommand(new Stats());
    }

    public void reloadVirusDatabases() {
        sendCommand(new Reload());
    }

    public void shutdownServer() {
        sendCommand(new Shutdown());
    }

    /**
     * Scans an <code>InputStream</code> and sends a response as soon as a virus has been found.
     * @param inputStream
     * @return
     */
    public ScanResult scan(InputStream inputStream) {
        return sendCommand(new InStream(inputStream));
    }

    /**
     * Scans a file/directory on the server filesystem and sends a response as soon as a virus has been found.
     *
     * @param path
     * @return
     */
    public ScanResult scan(Path path) {
        return scan(path, false);
    }

    /**
     * Scans a file/directory on the server filesystem and may continue the scan to the end
     * even if a virus has been found, depending on the <code>continueScan</code> argument.
     * @param path
     * @param continueScan
     * @return
     */
    public ScanResult scan(Path path, boolean continueScan) {
        if (continueScan) {
            return sendCommand(new ContScan(serverPlatform.toServerPath(path)));
        } else {
            return sendCommand(new Scan(serverPlatform.toServerPath(path)));
        }
    }

    /**
     * Scans a file/directory on the server filesystem and will continue the scan to the end
     * even if a virus has been found.
     * This method may improve performances by multi-threading the scan.
     * @param path
     * @return
     */
    public ScanResult parallelScan(Path path) {
        return sendCommand(new MultiScan(serverPlatform.toServerPath(path)));
    }

    private List<String> getAvailableCommands() throws IOException {
        if (availableCommands == null) {
            availableCommands = new VersionCommands().send(server);
        }

        return availableCommands;
    }

    private <T> T sendCommand(Command<T> command) {
        try {
            if (!getAvailableCommands().contains(command.getCommandString())) {
                throw new UnsupportedCommandException(command.getCommandString());
            }

            return command.send(server);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to send the %s command to the server",
                    command.getCommandString()),
                    e);
        }
    }
}
