package dev.fmsea.driver;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import dev.fmsea.driver.util.OrdererType;
import dev.fmsea.picotelem.engine.PicoTelemetryEngine;

public class AnalysisOptionsBuilder {
    private String className;
    private int methodId;
    private Path outputResultsPath;
    private boolean outputStateReports;
    private boolean outputConstraintTypes;
    private int widenIterations;
    private Class<?> stateType;
    private Optional<Set<Integer>> widenSteps;
    private OrdererType orderer;
    private boolean reduceOutput;
    private PicoTelemetryEngine telemetry;
    private boolean reportInflow;

    public AnalysisOptionsBuilder() {
        this.outputStateReports = true;
        this.outputConstraintTypes = false;
        this.widenIterations = 2;
        this.widenSteps = Optional.empty();
        this.orderer = OrdererType.PseudoTopological;
    }

    public AnalysisOptionsBuilder withClassName(String className) {
        this.className = className;
        return this;
    }

    public AnalysisOptionsBuilder withMethodId(int methodId) {
        this.methodId = methodId;
        return this;
    }

    public AnalysisOptionsBuilder withOutputResultsPath(Path outputResultsPath) {
        this.outputResultsPath = outputResultsPath;
        return this;
    }

    public AnalysisOptionsBuilder withOutputConstraintTypesReport(boolean flag) {
        this.outputConstraintTypes = flag;
        return this;
    }

    public AnalysisOptionsBuilder withOutputStateReports(boolean outputStateReports) {
        this.outputStateReports = outputStateReports;
        return this;
    }

    public AnalysisOptionsBuilder withStateType(Class<?> stateType) {
        this.stateType = stateType;
        return this;
    }

    public AnalysisOptionsBuilder withWidenIterations(int widenIterations) {
        this.widenIterations = widenIterations;
        return this;
    }

    public AnalysisOptionsBuilder withWidenSteps(Set<Integer> widenSteps) {
        this.widenSteps = Optional.ofNullable(widenSteps);
        return this;
    }

    public AnalysisOptionsBuilder withOrderer(OrdererType type) {
        this.orderer = type;
        return this;
    }

    public AnalysisOptionsBuilder withReduceOutput(boolean reduce) {
        this.reduceOutput = reduce;
        return this;
    }

    public AnalysisOptionsBuilder withTelemetry(PicoTelemetryEngine telemetry) {
        this.telemetry = telemetry;
        return this;
    }

    public AnalysisOptionsBuilder withReportInflow(boolean reportInflow) {
        this.reportInflow = reportInflow;
        return this;
    }

    public AnalysisOptions build() {
        return new AnalysisOptions(
            this.stateType,
            this.className,
            this.methodId,
            this.outputResultsPath,
            this.outputStateReports,
            this.outputConstraintTypes,
            this.widenIterations,
            this.widenSteps,
            this.orderer,
            this.reduceOutput,
            this.telemetry,
            this.reportInflow);
    }
}
