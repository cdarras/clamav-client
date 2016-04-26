package xyz.capybara.clamav;

import xyz.capybara.clamav.commands.scan.ContScan;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.configuration.Platform;
import xyz.capybara.clamav.exceptions.ClamavException;
import xyz.capybara.clamav.exceptions.UnsupportedCommandException;
import xyz.capybara.clamav.commands.Command;
import xyz.capybara.clamav.commands.Ping;
import xyz.capybara.clamav.commands.Reload;
import xyz.capybara.clamav.commands.Shutdown;
import xyz.capybara.clamav.commands.Stats;
import xyz.capybara.clamav.commands.Version;
import xyz.capybara.clamav.commands.VersionCommands;
import xyz.capybara.clamav.commands.scan.InStream;
import xyz.capybara.clamav.commands.scan.MultiScan;
import xyz.capybara.clamav.commands.scan.Scan;

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

    public void ping() throws ClamavException {
        sendCommand(new Ping());
    }

    public String version() throws ClamavException {
        return sendCommand(new Version());
    }

    public String stats() throws ClamavException {
        return sendCommand(new Stats());
    }

    public void reloadVirusDatabases() throws ClamavException {
        sendCommand(new Reload());
    }

    public void shutdownServer() throws ClamavException {
        sendCommand(new Shutdown());
    }

    /**
     * Scans an <code>InputStream</code> and sends a response as soon as a virus has been found.
     * @param inputStream
     * @return
     */
    public ScanResult scan(InputStream inputStream) throws ClamavException {
        return sendCommand(new InStream(inputStream));
    }

    /**
     * Scans a file/directory on the server filesystem and sends a response as soon as a virus has been found.
     *
     * @param path
     * @return
     */
    public ScanResult scan(Path path) throws ClamavException {
        return scan(path, false);
    }

    /**
     * Scans a file/directory on the server filesystem and may continue the scan to the end
     * even if a virus has been found, depending on the <code>continueScan</code> argument.
     * @param path
     * @param continueScan
     * @return
     */
    public ScanResult scan(Path path, boolean continueScan) throws ClamavException {
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
    public ScanResult parallelScan(Path path) throws ClamavException {
        return sendCommand(new MultiScan(serverPlatform.toServerPath(path)));
    }

    private List<String> getAvailableCommands() {
        if (availableCommands == null) {
            availableCommands = new VersionCommands().send(server);
        }
        return availableCommands;
    }

    private <T> T sendCommand(Command<T> command) throws ClamavException {
        try {
            if (getAvailableCommands().contains(command.getCommandString())) {
                return command.send(server);
            }
            throw new UnsupportedCommandException(command.getCommandString());
        } catch (Exception cause) {
            throw new ClamavException(cause);
        }
    }
}
