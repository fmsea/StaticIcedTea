package dev.fmsea.picotelem.factory;

import dev.fmsea.picotelem.engine.PicoTelemetryEngine;
import dev.fmsea.picotelem.sink.LogSink;

public class PicoTelemetryFactory {

    private static PicoTelemetryEngine instance;

    private PicoTelemetryFactory() {
    }

    public static PicoTelemetryEngine getTelemetryEngine() {
        if (instance == null) {
            LogSink sink = new LogSink();
            instance = new PicoTelemetryEngine(sink);
        }

        return instance;
    }
}
