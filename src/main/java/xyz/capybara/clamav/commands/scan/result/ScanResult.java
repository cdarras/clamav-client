package xyz.capybara.clamav.commands.scan.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * This class holds the result of an antivirus scan.
 * It contains a status and a map filled as following:
 * <ul>
 *     <li>Key: infected file path</li>
 *     <li>Value: list of viruses found in the file</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ScanResult {

    @NonNull
    private Status status;
    private Map<String, Collection<String>> foundViruses;

    public enum Status {
        OK, VIRUS_FOUND
    }
}
