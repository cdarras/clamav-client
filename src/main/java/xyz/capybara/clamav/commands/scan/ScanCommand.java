package xyz.capybara.clamav.commands.scan;

import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.exceptions.InvalidResponseException;
import xyz.capybara.clamav.exceptions.ScanFailureException;
import xyz.capybara.clamav.commands.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

public abstract class ScanCommand extends Command<ScanResult> {

    private static final Pattern RESPONSE_OK = Pattern.compile(
            "(.+) OK$",
            Pattern.UNIX_LINES
    );
    private static final Pattern RESPONSE_VIRUS_FOUND = Pattern.compile(
            "(.+) FOUND$",
            Pattern.MULTILINE & Pattern.UNIX_LINES
    );
    private static final Pattern RESPONSE_ERROR = Pattern.compile(
            "(.+) ERROR",
            Pattern.UNIX_LINES
    );
    private static final Pattern RESPONSE_VIRUS_FOUND_LINE = Pattern.compile(
            "(.+: )?(?<filePath>.+): (?<virus>.+) FOUND$",
            Pattern.UNIX_LINES
    );

    @Override
    protected ScanResult parseResponse(String responseString) {
        if (RESPONSE_OK.matcher(responseString).matches()) {
            return new ScanResult(ScanResult.Status.OK);
        }
        if (RESPONSE_VIRUS_FOUND.matcher(responseString).find()) {
            // add every found viruses to the scan result, grouped by infected file
            Map<String, Collection<String>> foundViruses = Arrays.stream(responseString.split("\n"))
                    .map(line -> {
                        Matcher matcher = RESPONSE_VIRUS_FOUND_LINE.matcher(line);
                        assert matcher.matches();
                        return new VirusInfo(matcher.group("filePath"), matcher.group("virus"));
                    })
                    .collect(new VirusInfoCollector());
            return new ScanResult(ScanResult.Status.VIRUS_FOUND, foundViruses);
        }
        if (RESPONSE_ERROR.matcher(responseString).matches()) {
            throw new ScanFailureException(responseString);
        }

        throw new InvalidResponseException(responseString);
    }

    @Getter
    @AllArgsConstructor
    private class VirusInfo {
        private String filePath, name;
    }

    private class VirusInfoCollector
            implements Collector<VirusInfo, Map<String, Collection<String>>, Map<String, Collection<String>>> {

        @Override
        public Supplier<Map<String, Collection<String>>> supplier() {
            return () -> new HashMap<>();
        }

        @Override
        public BiConsumer<Map<String, Collection<String>>, VirusInfo> accumulator() {
            // add every viruses found for each file
            return (map, virusInfo) ->
                    map.computeIfAbsent(virusInfo.getFilePath(), key -> new ArrayList<>()).add(virusInfo.getName());
        }

        @Override
        public BinaryOperator<Map<String, Collection<String>>> combiner() {
            // add every entries of the second map to the first
            return (left, right) -> {
                right.forEach((file, viruses) ->
                        viruses.forEach(name ->
                                left.computeIfAbsent(file, key -> new ArrayList<>()).add(name)));
                return left;
            };
        }

        @Override
        public Function<Map<String, Collection<String>>, Map<String, Collection<String>>> finisher() {
            // identity transform
            return (map -> map);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
        }
    }
}
