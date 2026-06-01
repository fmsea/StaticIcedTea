package dev.fmsea.absint.scalar;

import java.util.Optional;
import java.util.Set;

import dev.fmsea.picotelem.PicoTelemOptionsBuilder;
import dev.fmsea.picotelem.engine.PicoTelemetryEngine;
import dev.fmsea.picotelem.factory.PicoTelemetryFactory;
import dev.fmsea.solver.SolverFactory;
import dev.fmsea.solver.SolverWrapper;
import soot.toolkits.graph.Orderer;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.Body;
import soot.Unit;

public class IntegerAnalysisBuilder {
    private int iterations;
    private Body b;
    private Optional<SolverWrapper> solver = Optional.empty();
    private boolean reduceOutput = true;
    private Class<?> type;
    private Optional<Set<Integer>> widenSteps = Optional.empty();
    private Optional<Orderer<Unit>> orderer = Optional.empty();
    private Optional<PicoTelemetryEngine> telemetry = Optional.empty();

    public IntegerAnalysisBuilder() {
    }

    public IntegerAnalysisBuilder withBody(Body body) {
        this.b = body;
        return this;
    }

    public IntegerAnalysisBuilder withIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public IntegerAnalysisBuilder withSolver(SolverWrapper solver) {
        this.solver = Optional.ofNullable(solver);
        return this;
    }

    public IntegerAnalysisBuilder withReducedOutput(boolean reduceOutput) {
        this.reduceOutput = reduceOutput;
        return this;
    }

    public IntegerAnalysisBuilder withType(Class<?> type) {
        this.type = type;
        return this;
    }

    public IntegerAnalysisBuilder withTelemetry(PicoTelemetryEngine telemetry) {
        this.telemetry = Optional.ofNullable(telemetry);
        return this;
    }

    public IntegerAnalysis build() {
        return new IntegerAnalysis(
            this.solver.orElse(SolverFactory.getSolver()),
            this.b,
            this.iterations,
            this.type,
            this.widenSteps.orElse(Set.of()),
            this.orderer.orElse(new PseudoTopologicalOrderer<>()),
            this.reduceOutput,
            this.telemetry.orElse(PicoTelemetryFactory.getTelemetryEngine(new PicoTelemOptionsBuilder().build())));
    }
}
