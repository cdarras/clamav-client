package xyz.capybara.commands.scan.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

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
