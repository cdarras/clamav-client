package xyz.capybara.clamav.commands.scan

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import xyz.capybara.clamav.InvalidOptionValueException
import java.io.InputStream
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class InStreamTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(SocketChannel::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SocketChannel::class)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 10000])
    fun `should use the configured chunkSize when sending the request to ClamAV`(chunkSize: Int) {
        //given
        val inputStream = mockk<InputStream>()
        val command = InStream(inputStream, chunkSize)

        val bufferWrites = prepareMocks(inputStream)

        //when
        runCatching { command.send(mockk()) } //don't care about the exceptions

        //then
        //1 write is the command, 2 write is the chunk size, 3rd write is the chunk
        assertThat(bufferWrites[2].array().size).isEqualTo(chunkSize)
    }

    @Test
    fun `should use the default chunkSize when chunkSize is not configured`() {
        //given
        val inputStream = mockk<InputStream>()
        val command = InStream(inputStream)

        val bufferWrites = prepareMocks(inputStream)

        //when
        runCatching { command.send(mockk()) } //don't care about the exceptions

        //then
        //1 write is the command, 2 write is the chunk size, 3rd write is the chunk
        assertThat(bufferWrites[2].array().size).isEqualTo(InStream.DEFAULT_CHUNK_SIZE)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `should throw exception if chunkSize is less than or equal to 0`(chunkSize: Int) {
        //given
        //when
        //then
        assertThatExceptionOfType(InvalidOptionValueException::class.java).isThrownBy { InStream(mockk(), chunkSize) }
    }

    private fun prepareMocks(inputStream: InputStream): MutableList<ByteBuffer> {
        every { inputStream.read(any()) } answers { firstArg<ByteArray>().size } andThen -1

        val bufferWrites = mutableListOf<ByteBuffer>()
        every { SocketChannel.open(any<SocketAddress>()) } returns mockk<SocketChannel> {
            every { write(capture(bufferWrites)) } answers {
                firstArg<ByteBuffer>().run {
                    val consumed = limit() - position() - 1
                    position(consumed + 1)
                    consumed
                }
            }
            every { read(any<ByteBuffer>()) } returns -1
            every { close() } just Runs
        }
        return bufferWrites
    }
}