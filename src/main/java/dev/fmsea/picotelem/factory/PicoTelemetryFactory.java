package dev.fmsea.picotelem.factory;

import java.nio.file.Path;

import dev.fmsea.picotelem.PicoTelemOptions;
import dev.fmsea.picotelem.PicoTelemOptionsBuilder;
import dev.fmsea.picotelem.engine.PicoTelemetryEngine;
import dev.fmsea.picotelem.sink.LogSink;
import dev.fmsea.picotelem.sink.RoutingSink;
import dev.fmsea.picotelem.sink.TsvFileSink;

public class PicoTelemetryFactory {

    private static PicoTelemetryEngine instance;

    private PicoTelemetryFactory() {
    }

    public static PicoTelemetryEngine getTelemetryEngine() {
        return getTelemetryEngine(new PicoTelemOptionsBuilder().build());
    }

    public static PicoTelemetryEngine getTelemetryEngine(PicoTelemOptions options) {
        if (instance == null) {
            LogSink sink = new LogSink();
            RoutingSink route = new RoutingSink(sink);
            options.analysisEventsFile.ifPresent(filename -> {
                route.routePrefix("analysis", new TsvFileSink(Path.of(filename)));
            });
            instance = new PicoTelemetryEngine(route);
        }

        return instance;
    }
}
