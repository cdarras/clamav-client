package xyz.capybara.clamav.configuration;

import java.io.File;
import java.nio.file.Path;

public enum Platform {
    UNIX('/'),
    WINDOWS('\\');

    private char separator;

    Platform(char separator) {
        this.separator = separator;
    }

    public String toServerPath(Path path) {
        return path.toString().replace(File.separatorChar, separator);
    }
}
