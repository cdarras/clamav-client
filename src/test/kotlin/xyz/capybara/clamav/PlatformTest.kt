package xyz.capybara.clamav


import java.nio.file.Paths

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class PlatformTest {

    @Test
    fun `should convert file separator to UNIX`() {
        // Given
        val platform = Platform.UNIX
        val originalPath = Paths.get("test\\path\\to\\file")
        // When
        val convertedPath = platform.toServerPath(originalPath)
        // Then
        then(convertedPath).isEqualTo("test/path/to/file")
    }

    @Test
    fun `should convert file separator to WINDOWS`() {
        // Given
        val platform = Platform.WINDOWS
        val originalPath = Paths.get("test/path/to/file")
        // When
        val convertedPath = platform.toServerPath(originalPath)
        // Then
        then(convertedPath).isEqualTo("test\\path\\to\\file")
    }

    @Test
    fun `should keep JVM platform file separator`() {
        // Given
        val platform = Platform.JVM_PLATFORM
        val originalPath = Paths.get("test/path/to/file")
        // When
        val convertedPath = platform.toServerPath(originalPath)
        // Then
        then(convertedPath).isEqualTo(originalPath.toString())
    }
}
