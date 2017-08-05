package xyz.capybara.clamav

import xyz.capybara.clamav.commands.*
import xyz.capybara.clamav.commands.scan.ContScan
import xyz.capybara.clamav.commands.scan.InStream
import xyz.capybara.clamav.commands.scan.MultiScan
import xyz.capybara.clamav.commands.scan.Scan
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.file.Path

/**
 * Kotlin ClamAV Client
 */
class ClamavClient
/**
 * Creates a ClamavClient which will connect to the ClamAV daemon on the given socket address running on the given platform.
 *
 * @param server         Server socket address (IP address and port or hostname and port)
 * @param serverPlatform Server platform (determines the file path separator to use when launching a file/directory scan on the server filesystem)
 */
@JvmOverloads constructor(val server: InetSocketAddress,
                          val serverPlatform: Platform = ClamavClient.DEFAULT_SERVER_PLATFORM) {
    /**
     * Creates a ClamavClient which will connect to the ClamAV daemon on the given hostname running on the given platform.
     *
     * Default values:
     *  Port: 3310
     *
     * @param serverHostname Server hostname
     * @param serverPlatform Server platform (determines the file path separator to use when launching a file/directory scan on the server filesystem)
     */
    constructor(serverHostname: String, serverPlatform: Platform) : this(serverHostname, DEFAULT_SERVER_PORT, serverPlatform)

    /**
     * Creates a ClamavClient which will connect to the ClamAV daemon on the given hostname and port running on the given platform.
     *
     * @param serverHostname Server hostname
     * @param serverPort     Server port
     * @param serverPlatform Server platform (determines the file path separator to use when launching a file/directory scan on the server filesystem)
     */
    @JvmOverloads constructor(serverHostname: String,
                              serverPort: Int = DEFAULT_SERVER_PORT,
                              serverPlatform: Platform = DEFAULT_SERVER_PLATFORM) : this(InetSocketAddress(serverHostname, serverPort), serverPlatform)

    private val availableCommands: Collection<String> by lazy { VersionCommands.send(server) }

    /**
     * Pings the ClamAV daemon. If a correct response has been received, the method simply returns.
     * Otherwise, a [ClamavException] is thrown.
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun ping() = sendCommand(Ping)

    /**
     * Requests the version of the ClamAV daemon
     *
     * @return version of the ClamAV daemon
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun version(): String = sendCommand(Version)

    /**
     * Requests stats from the ClamAV daemon
     *
     * @return multilined String holding various stats given by the ClamAV daemon
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun stats(): String = sendCommand(Stats)

    /**
     * Triggers the virus databases reloading by the ClamAV daemon
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun reloadVirusDatabases() = sendCommand(Reload)

    /**
     * Immediately shutdowns the ClamAV daemon
     *
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun shutdownServer() = sendCommand(Shutdown)

    /**
     * Scans an `InputStream` and sends a response as soon as a virus has been found.
     *
     * @param inputStream inputStream to scan
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun scan(inputStream: InputStream): ScanResult = sendCommand(InStream(inputStream))

    /**
     * Scans a file/directory on the filesystem of the ClamAV daemon and may continue the scan to the end
     * even if a virus has been found, depending on the `continueScan` argument.
     *
     * @param path         absolute path to the file/directory on the filesystem of the ClamAV daemon
     * @param continueScan continue the scan to the end even if the virus has been found
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    @JvmOverloads fun scan(path: Path, continueScan: Boolean = false): ScanResult =
            sendCommand(if (continueScan) ContScan(serverPlatform.toServerPath(path)) else Scan(serverPlatform.toServerPath(path)))

    /**
     * Scans a file/directory on the filesystem of the ClamAV daemon and will continue the scan to the end
     * even if a virus has been found.
     * This method may improve performances on SMP systems by performing a multi-threaded scan.
     *
     * @param path absolute path to the file/directory on the filesystem of the ClamAV daemon
     * @return result of the scan
     * @throws ClamavException Exception holding the real cause of malfunction
     */
    @Throws(ClamavException::class)
    fun parallelScan(path: Path): ScanResult = sendCommand(MultiScan(serverPlatform.toServerPath(path)))

    @Throws(ClamavException::class)
    private fun <T> sendCommand(command: Command<T>): T {
        try {
            if (command.commandString in availableCommands) {
                return command.send(server)
            }
            throw UnsupportedCommandException(command.commandString)
        } catch (cause: RuntimeException) {
            throw ClamavException(cause)
        }
    }

    companion object {
        @JvmField
        val DEFAULT_SERVER_PORT = 3310
        @JvmField
        val DEFAULT_SERVER_PLATFORM = Platform.JVM_PLATFORM
    }
}

/**
 * This enum is used to determine the file separator char which will be used when building the path
 * to the file/directory to scan on the server filesystem. The possible values are:
 *
 *  * UNIX: the file separator will be `/`
 *  * WINDOWS: the file separator will be `\`
 *  * JVM_PLATFORM: the file separator will be the same as the one of the platform the JVM is running on
 *
 */
enum class Platform(private val separator: Char) {
    UNIX('/') {
        override fun toServerPath(path: Path): String = path.toString().replace(WINDOWS.separator, UNIX.separator)
    },
    WINDOWS('\\') {
        override fun toServerPath(path: Path): String = path.toString().replace(UNIX.separator, WINDOWS.separator)
    },
    JVM_PLATFORM(File.separatorChar) {
        override fun toServerPath(path: Path): String = path.toString()
    };

    abstract fun toServerPath(path: Path): String
}