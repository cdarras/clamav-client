package xyz.capybara.clamav


import java.net.InetSocketAddress

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ClamavClientTest {

    @Test
    fun `should create a ClamavClient for the given hostname`() {
        // Given
        val hostname = "localhost"
        // When
        val client = ClamavClient(hostname)
        // Then
        then(client.server).isEqualTo(InetSocketAddress(hostname, ClamavClient.DEFAULT_SERVER_PORT))
        then(client.serverPlatform).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the given hostname and port`() {
        // Given
        val hostname = "localhost"
        val port = 3311
        // When
        val client = ClamavClient(hostname, port)
        // Then
        then(client.server).isEqualTo(InetSocketAddress(hostname, port))
        then(client.serverPlatform).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the given hostname and platform`() {
        // Given
        val hostname = "localhost"
        val platform = Platform.UNIX
        // When
        val client = ClamavClient(hostname, platform)
        // Then
        then(client.server).isEqualTo(InetSocketAddress(hostname, ClamavClient.DEFAULT_SERVER_PORT))
        then(client.serverPlatform).isEqualTo(platform)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the hostname, port and platform`() {
        // Given
        val hostname = "localhost"
        val port = 3311
        val platform = Platform.UNIX
        // When
        val client = ClamavClient(hostname, port, platform)
        // Then
        then(client.server).isEqualTo(InetSocketAddress(hostname, port))
        then(client.serverPlatform).isEqualTo(platform)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the given socket address`() {
        // Given
        val socketAddress = InetSocketAddress("localhost", 3311)
        // When
        val client = ClamavClient(socketAddress)
        // Then
        then(client.server).isEqualTo(socketAddress)
        then(client.serverPlatform).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the given socket address and platform`() {
        // Given
        val socketAddress = InetSocketAddress("localhost", 3311)
        val platform = Platform.UNIX
        // When
        val client = ClamavClient(socketAddress, platform)
        // Then
        then(client.server).isEqualTo(socketAddress)
        then(client.serverPlatform).isEqualTo(platform)
        then(client.timeout).isEqualTo(ClamavClient.DEFAULT_TIMEOUT);
    }

    @Test
    fun `should create a ClamavClient for the given socket address, platform and timeout`() {
        // Given
        val socketAddress = InetSocketAddress("localhost", 3311)
        val platform = Platform.UNIX
        val timeout = 500;
        // When
        val client = ClamavClient(socketAddress, platform, timeout)
        // Then
        then(client.server).isEqualTo(socketAddress)
        then(client.serverPlatform).isEqualTo(platform)
        then(client.timeout).isEqualTo(timeout);
    }
}
