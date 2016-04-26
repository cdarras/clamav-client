package xyz.capybara.clamav;

import lombok.Getter;
import xyz.capybara.clamav.commands.*;
import xyz.capybara.clamav.commands.Shutdown;
import xyz.capybara.clamav.commands.scan.ContScan;
import xyz.capybara.clamav.commands.scan.InStream;
import xyz.capybara.clamav.commands.scan.MultiScan;
import xyz.capybara.clamav.commands.scan.Scan;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.configuration.Platform;
import xyz.capybara.clamav.exceptions.ClamavException;
import xyz.capybara.clamav.exceptions.UnsupportedCommandException;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Java ClamAV Client
 */
public class ClamavClient {
    public static final int DEFAULT_SERVER_PORT = 3310;
    public static final Platform DEFAULT_SERVER_PLATFORM = Platform.JVM_PLATFORM;

    @Getter
    private InetSocketAddress server;
    @Getter
    private Platform serverPlatform;
    private Collection<String> availableCommands;

    /**+
     * Creates a ClamavClient which will connect to ClamAV on the given hostname.
     * Default values:
     * <ul>
     *     <li>Port: 3310</li>
     *     <li>Platform: the one the JVM is running on</li>
     * </ul>
     *
     * @param serverHostname Server hostname
     */
    public ClamavClient(String serverHostname) {
        this(serverHostname, DEFAULT_SERVER_PORT);
    }

    /**
     * Creates a ClamavClient which will connect to ClamAV on the given hostname and port.
     * Default values:
     * <ul>
     *     <li>Platform: the one the JVM is running on</li>
     * </ul>
     *
     * @param serverHostname Server hostname
     * @param serverPort     Server port
     */
    public ClamavClient(String serverHostname, int serverPort) {
        this(serverHostname, serverPort, DEFAULT_SERVER_PLATFORM);
    }

    /**
     * Creates a ClamavClient which will connect to ClamAV on the given hostname running on the given platform.
     * Default values:
     * <ul>
     *     <li>Port: 3310</li>
     * </ul>
     *
     * @param serverHostname Server hostname
     * @param serverPlatform Server platform
     *                       (determines the file path separator to use when launching a file/directory scan
     *                       on the server filesystem)
     */
    public ClamavClient(String serverHostname, Platform serverPlatform) {
        this(serverHostname, DEFAULT_SERVER_PORT, serverPlatform);
    }

    /**
     * Creates a ClamavClient which will connect to ClamAV on the given hostname and port running on the given platform.
     *
     * @param serverHostname Server hostname
     * @param serverPort     Server port
     * @param serverPlatform Server platform
     *                       (determines the file path separator to use when launching a file/directory scan
     *                       on the server filesystem)
     */
    public ClamavClient(String serverHostname, int serverPort, Platform serverPlatform) {
        this(new InetSocketAddress(serverHostname, serverPort), serverPlatform);
    }

    /**
     * Creates a ClamavClient which will connect to ClamAV on the given socket address.
     * Default values:
     * <ul>
     *     <li>Platform: the one the JVM is running on</li>
     * </ul>
     *
     * @param server Server socket address (IP address and port or hostname and port)
     */
    public ClamavClient(InetSocketAddress server) {
        this(server, DEFAULT_SERVER_PLATFORM);
    }

    /**
     * Creates a ClamavClient which will connect to ClamAV on the given socket address running on the given platform.
     *
     * @param server         Server socket address (IP address and port or hostname and port)
     * @param serverPlatform Server platform
     *                       (determines the file path separator to use when launching a file/directory scan
     *                       on the server filesystem)
     */
    public ClamavClient(InetSocketAddress server, Platform serverPlatform) {
        this.server = server;
        this.serverPlatform = serverPlatform;
    }

    /**
     * Pings the ClamAV service. If a correct response has been sent, the method simply returns.
     * Otherwise, an exception is thrown.
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public void ping() throws ClamavException {
        sendCommand(new Ping());
    }

    /**
     * Requests the version of the ClamAV service
     *
     * @return version of the ClamAV service
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public String version() throws ClamavException {
        return sendCommand(new Version());
    }

    /**
     * Requests stats from the ClamAV service
     *
     * @return multilined String holding various stats given by the ClamAV service
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public String stats() throws ClamavException {
        return sendCommand(new Stats());
    }

    /**
     *
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public void reloadVirusDatabases() throws ClamavException {
        sendCommand(new Reload());
    }

    /**
     * Shutdowns the ClamAV service on the server
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public void shutdownServer() throws ClamavException {
        sendCommand(new Shutdown());
    }

    /**
     * Scans an <code>InputStream</code> and sends a response as soon as a virus has been found.
     *
     * @param inputStream inputStream to scan
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public ScanResult scan(InputStream inputStream) throws ClamavException {
        return sendCommand(new InStream(inputStream));
    }

    /**
     * Scans a file/directory on the server filesystem and sends a response as soon as a virus has been found.
     *
     * @param path absolute path to the file/directory on the server
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public ScanResult scan(Path path) throws ClamavException {
        return scan(path, false);
    }

    /**
     * Scans a file/directory on the server filesystem and may continue the scan to the end
     * even if a virus has been found, depending on the <code>continueScan</code> argument.
     *
     * @param path         absolute path to the file/directory on the server
     * @param continueScan continue the scan to the end even if the virus has been found
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
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
     * This method may improve performances on SMP systems by performing a multi-threaded scan.
     *
     * @param path absolute path to the file/directory on the server
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    public ScanResult parallelScan(Path path) throws ClamavException {
        return sendCommand(new MultiScan(serverPlatform.toServerPath(path)));
    }

    private Collection<String> getAvailableCommands() {
        if (availableCommands == null) {
            availableCommands = new VersionCommands().send(server);
        }
        return availableCommands;
    }

    private <T> T sendCommand(Command<T> command) throws ClamavException {
        try {
            if (getAvailableCommands() != null && getAvailableCommands().contains(command.getCommandString())) {
                return command.send(server);
            }
            throw new UnsupportedCommandException(command.getCommandString());
        } catch (Exception cause) {
            throw new ClamavException(cause);
        }
    }
}
