package xyz.capybara.clamav.commands.scan

import xyz.capybara.clamav.commands.connect
import xyz.capybara.clamav.commands.scan.result.ScanResult
import xyz.capybara.clamav.CommunicationException
import xyz.capybara.clamav.InvalidOptionValueException

import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel

internal class InStream(private val inputStream: InputStream,
                        chunkSize : Int = DEFAULT_CHUNK_SIZE) : ScanCommand() {

    override val commandString
        get() = "INSTREAM"

    override val format
        get() = CommandFormat.NULL_CHAR

    private val chunkSize = chunkSize.takeIf { it > 0 }
        ?: throw InvalidOptionValueException(commandString, "chunkSize", "must be greater than 0")

    override fun send(server: InetSocketAddress, timeoutMillis: Int): ScanResult {
        try {
            SocketChannel.open().use { channel ->
                channel.connect(server, timeoutMillis)
                channel.write(rawCommand)

                // ByteBuffer order must be big-endian ( == network byte order)
                // It is, by default, but it doesn't hurt to set it anyway
                val length = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
                val data = ByteArray(chunkSize)
                var chunkSize = chunkSize
                while (chunkSize != -1) {
                    chunkSize = inputStream.read(data)
                    if (chunkSize > 0) {
                        (length as Buffer).clear()
                        (length.putInt(chunkSize) as Buffer).flip()
                        // The format of the chunk is: '<length><data>'
                        channel.write(length)
                        channel.write(ByteBuffer.wrap(data, 0, chunkSize))
                    }
                }
                (length as Buffer).clear()
                // Terminate the stream by sending a zero-length chunk
                (length.putInt(0) as Buffer).flip()
                channel.write(length)

                return readResponse(channel)
            }
        } catch (e: IOException) {
            throw CommunicationException(e)
        }
    }

    companion object {
        const val DEFAULT_CHUNK_SIZE = 2048
    }
}
