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
import kotlin.jvm.Throws

internal abstract class Command<out T> {
    abstract val commandString: String

    open fun send(server: InetSocketAddress): T {
        try {
            SocketChannel.open(server).use {
                it.write(rawCommand)
                return readResponse(it)
            }
        } catch (e: IOException) {
            throw CommunicationException(e)
        }
    }

    protected abstract val format: CommandFormat

    open protected val rawCommand: ByteBuffer
        get() = ByteBuffer.wrap("${format.prefix}$commandString${format.terminator}".toByteArray(StandardCharsets.US_ASCII))

    @Throws(IOException::class)
    protected fun readResponse(socketChannel: SocketChannel): T {
        val responseStringBuilder = StringBuilder()
        var rawResponsePart = ByteBuffer.allocate(32)
        var read = socketChannel.read(rawResponsePart)
        while (read > -1) {
            var rawResponsePartString = String(rawResponsePart.array(), StandardCharsets.US_ASCII)
            rawResponsePartString = rawResponsePartString.substring(0, read)
            responseStringBuilder.append(rawResponsePartString)
            rawResponsePart = ByteBuffer.allocate(32)
            read = socketChannel.read(rawResponsePart)
        }
        val responseString = removeResponseTerminator(responseStringBuilder.toString())
        if (responseString == "UNKNOWN COMMAND") {
            throw UnknownCommandException(commandString)
        }
        logger.debug { "$commandString - Response: $responseString" }
        return parseResponse(responseString)
    }

    private fun removeResponseTerminator(responseString: String) = responseString.substring(0, responseString.lastIndexOf(format.terminator))

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
