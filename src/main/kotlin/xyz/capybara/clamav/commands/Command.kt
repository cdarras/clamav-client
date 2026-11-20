package xyz.capybara.clamav.commands

import mu.KLogging
import xyz.capybara.clamav.ClamavException
import xyz.capybara.clamav.CommunicationException
import xyz.capybara.clamav.UnknownCommandException
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

/** No timeout: blocking connect and read without SO_TIMEOUT (previous default behavior). */
internal const val NO_TIMEOUT = -1

/**
 * Connects [this] channel to [server], bounded by [timeoutMillis] for both the connect and the
 * read phase (SO_TIMEOUT on the blocking socket). With [NO_TIMEOUT] it behaves as before.
 */
internal fun SocketChannel.connect(server: InetSocketAddress, timeoutMillis: Int) {
    if (timeoutMillis > 0) {
        socket().soTimeout = timeoutMillis
        socket().connect(server, timeoutMillis)
    } else {
        connect(server)
    }
}

internal abstract class Command<out T> {
    abstract val commandString: String

    open fun send(server: InetSocketAddress, timeoutMillis: Int = NO_TIMEOUT): T {
        try {
            SocketChannel.open().use { channel ->
                channel.connect(server, timeoutMillis)
                channel.write(rawCommand)
                return readResponse(channel)
            }
        } catch (e: IOException) {
            throw CommunicationException(e)
        }
    }

    protected abstract val format: CommandFormat

    protected open val rawCommand: ByteBuffer
        get() = ByteBuffer.wrap("${format.prefix}$commandString${format.terminator}".toByteArray(StandardCharsets.UTF_8))

    @Throws(IOException::class)
    protected fun readResponse(socketChannel: SocketChannel): T {
        var readByteBuffer = ByteBuffer.allocate(32)
        var responseByteArray = ByteArray(0)

        var readSize = socketChannel.read(readByteBuffer)
        while (readSize > -1) {
            var readByteArray = readByteBuffer.array()
            if (readSize < 32) {
                readByteArray = readByteArray.sliceArray(0 until readSize)
            }
            responseByteArray = responseByteArray.plus(readByteArray)

            readByteBuffer = ByteBuffer.allocate(32)
            readSize = socketChannel.read(readByteBuffer)
        }

        val responseString = removeResponseTerminator(responseByteArray.decodeToString())
        if (responseString == "UNKNOWN COMMAND") {
            throw UnknownCommandException(commandString)
        }
        logger.debug { "$commandString - Response: $responseString" }
        return parseResponse(responseString)
    }

    private fun removeResponseTerminator(responseString: String) = responseString.substringBeforeLast(format.terminator)

    protected abstract fun parseResponse(responseString: String): T

    enum class CommandFormat(val prefix: Char, val terminator: Char) {
        NULL_CHAR('z', '\u0000'),
        NEW_LINE('n', '\n');

        companion object {
            fun fromPrefix(prefix: Char): CommandFormat = when(prefix) {
                'z' -> NULL_CHAR
                'n' -> NEW_LINE
                else -> throw ClamavException(IllegalArgumentException(prefix.toString()))
            }
        }
    }

    companion object: KLogging()
}
