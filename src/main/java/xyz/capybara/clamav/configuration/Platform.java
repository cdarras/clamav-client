package xyz.capybara.clamav.configuration;

import java.io.File;
import java.nio.file.Path;

/**
 * This enum is used to determine the file separator char which will be used when building the path
 * to the file/directory to scan on the server filesystem. The possible values are:
 * <ul>
 *     <li>UNIX: the file separator will be <code>/</code></li>
 *     <li>WINDOWS: the file separator will be <code>\</code></li>
 *     <li>JVM_PLATFORM: the file separator will be the same as the one of the platform the JVM is running on</li>
 * </ul>
 */
public enum Platform {
    UNIX('/'),
    WINDOWS('\\'),
    JVM_PLATFORM(File.separatorChar);

    private char separator;

    Platform(char separator) {
        this.separator = separator;
    }

    public String toServerPath(Path path) {
        if (this == UNIX) {
            return path.toString().replace(WINDOWS.separator, UNIX.separator);
        } else if (this == WINDOWS) {
            return path.toString().replace(UNIX.separator, WINDOWS.separator);
        }
        return path.toString();
    }
}
