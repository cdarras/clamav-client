package xyz.capybara.clamav;

import org.junit.Test;
import xyz.capybara.clamav.configuration.Platform;

import java.net.InetSocketAddress;

import static org.assertj.core.api.BDDAssertions.then;

public class ClamavClientTest {

    @Test
    public void should_create_ClamavClient_for_hostname() {
        // Given
        String hostname = "localhost";
        // When
        ClamavClient client = new ClamavClient(hostname);
        // Then
        then(client.getServer()).isEqualTo(new InetSocketAddress(hostname, ClamavClient.DEFAULT_SERVER_PORT));
        then(client.getServerPlatform()).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM);
    }

    @Test
    public void should_create_ClamavClient_for_hostname_and_port() {
        // Given
        String hostname = "localhost";
        int port = 3311;
        // When
        ClamavClient client = new ClamavClient(hostname, port);
        // Then
        then(client.getServer()).isEqualTo(new InetSocketAddress(hostname, port));
        then(client.getServerPlatform()).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM);
    }

    @Test
    public void should_create_ClamavClient_for_hostname_and_platform() {
        // Given
        String hostname = "localhost";
        Platform platform = Platform.UNIX;
        // When
        ClamavClient client = new ClamavClient(hostname, platform);
        // Then
        then(client.getServer()).isEqualTo(new InetSocketAddress(hostname, ClamavClient.DEFAULT_SERVER_PORT));
        then(client.getServerPlatform()).isEqualTo(platform);
    }

    @Test
    public void should_create_ClamavClient_for_hostname_and_port_and_platform() {
        // Given
        String hostname = "localhost";
        int port = 3311;
        Platform platform = Platform.UNIX;
        // When
        ClamavClient client = new ClamavClient(hostname, port, platform);
        // Then
        then(client.getServer()).isEqualTo(new InetSocketAddress(hostname, port));
        then(client.getServerPlatform()).isEqualTo(platform);
    }

    @Test
    public void should_create_ClamavClient_for_socket_address() {
        // Given
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 3311);
        // When
        ClamavClient client = new ClamavClient(socketAddress);
        // Then
        then(client.getServer()).isEqualTo(socketAddress);
        then(client.getServerPlatform()).isEqualTo(ClamavClient.DEFAULT_SERVER_PLATFORM);
    }

    @Test
    public void should_create_ClamavClient_for_socket_address_and_platform() {
        // Given
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 3311);
        Platform platform = Platform.UNIX;
        // When
        ClamavClient client = new ClamavClient(socketAddress, platform);
        // Then
        then(client.getServer()).isEqualTo(socketAddress);
        then(client.getServerPlatform()).isEqualTo(platform);
    }
}