package driver;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import abstractinterp.scalar.state.State;
import driver.util.OrdererType;

public class AnalysisOptions {

    public final Class<?> stateType;
    public final String className;
    public final int methodId;
    public final Path outputResultsPath;
    public final boolean outputStateReports;
    public final int widenIterations;
    public final Optional<Set<Integer>> widenSteps;
    public final OrdererType orderer;

    public AnalysisOptions(
        Class<?> stateType,
        String className,
        int methodId,
        Path outputResultsPath,
        boolean outputStateReports,
        int widenIterations,
        Optional<Set<Integer>> widenSteps,
        OrdererType orderer) {

        this.stateType = stateType;
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.outputStateReports = outputStateReports;
        this.widenIterations = widenIterations;
        this.widenSteps = widenSteps;
        this.orderer = orderer;
    }
}
