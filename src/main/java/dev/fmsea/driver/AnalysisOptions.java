package dev.fmsea.driver;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import dev.fmsea.driver.util.OrdererType;
import dev.fmsea.picotelem.engine.PicoTelemetryEngine;

public class AnalysisOptions {

    public final Class<?> stateType;
    public final String className;
    public final int methodId;
    public final Path outputResultsPath;
    public final boolean outputStateReports;
    public final boolean outputConstraintTypes;
    public final int widenIterations;
    public final Optional<Set<Integer>> widenSteps;
    public final OrdererType orderer;
    public final boolean reduceOutput;
    public final PicoTelemetryEngine telemetry;
    public final boolean reportInflow;

    public AnalysisOptions(
        Class<?> stateType,
        String className,
        int methodId,
        Path outputResultsPath,
        boolean outputStateReports,
        boolean outputConstraintTypes,
        int widenIterations,
        Optional<Set<Integer>> widenSteps,
        OrdererType orderer,
        boolean reduceOutput,
        PicoTelemetryEngine telemetry,
        boolean reportInflow) {

        this.stateType = stateType;
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.outputStateReports = outputStateReports;
        this.outputConstraintTypes = outputConstraintTypes;
        this.widenIterations = widenIterations;
        this.widenSteps = widenSteps;
        this.orderer = orderer;
        this.reduceOutput = reduceOutput;
        this.telemetry = telemetry;
        this.reportInflow = reportInflow;
    }
}
