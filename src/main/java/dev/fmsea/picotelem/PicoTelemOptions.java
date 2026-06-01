package dev.fmsea.picotelem;

import java.util.Optional;

public class PicoTelemOptions {
    public final Optional<String> analysisEventsFile;

    public PicoTelemOptions(String analysisEventsFile) {
        this.analysisEventsFile = Optional.ofNullable(analysisEventsFile);
    }
}
