package xyz.capybara.clamav.configuration;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.*;

public class PlatformTest {

    @Test
    public void should_convert_file_separator_to_UNIX() {
        // Given
        Platform platform = Platform.UNIX;
        Path originalPath = Paths.get("test\\path\\to\\file");
        // When
        String convertedPath = platform.toServerPath(originalPath);
        // Then
        then(convertedPath).isEqualTo("test/path/to/file");
    }

    @Test
    public void should_convert_file_separator_to_WINDOWS() {
        // Given
        Platform platform = Platform.WINDOWS;
        Path originalPath = Paths.get("test/path/to/file");
        // When
        String convertedPath = platform.toServerPath(originalPath);
        // Then
        then(convertedPath).isEqualTo("test\\path\\to\\file");
    }

    @Test
    public void should_keep_jvm_platform_file_separator() {
        // Given
        Platform platform = Platform.JVM_PLATFORM;
        Path originalPath = Paths.get("test/path/to/file");
        // When
        String convertedPath = platform.toServerPath(originalPath);
        // Then
        then(convertedPath).isEqualTo(originalPath.toString());
    }
}