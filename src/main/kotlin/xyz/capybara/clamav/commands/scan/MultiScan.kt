package xyz.capybara.clamav.commands.scan

import xyz.capybara.clamav.commands.Command

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class MultiScan(private val path: String) : ScanCommand() {
    override val commandString: String
        get() = "MULTISCAN"

    override val format: Command.CommandFormat
        get() = Command.CommandFormat.NEW_LINE

    override val rawCommand: ByteBuffer
        get() = ByteBuffer.wrap("${format.prefix}$commandString $path${format.terminator}".toByteArray(StandardCharsets.US_ASCII))
}
