package dev.fmsea.driver.commands;

import java.nio.file.Path;

import dev.fmsea.absint.scalar.state.DeferredOctagonState;
import dev.fmsea.driver.AnalysisOptions;
import dev.fmsea.driver.AnalysisOptionsBuilder;
import dev.fmsea.driver.AnalysisRunner;
import dev.fmsea.driver.util.SootInitialization;
import dev.fmsea.picotelem.PicoTelemOptions;
import dev.fmsea.picotelem.PicoTelemOptionsBuilder;
import dev.fmsea.picotelem.factory.PicoTelemetryFactory;
import dev.fmsea.util.Properties;
import picocli.CommandLine.Command;

@Command(name = "deferred-octagon-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Octagon Analysis")
public class StartDeferredOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Properties.OutputMinimumChangedVariables = outputMinimum;
        PicoTelemOptions telemetryOptions = new PicoTelemOptionsBuilder()
            .withAnalysisEventsFile(
                Path.of(outputResultsPath.toString(),
                    String.format("%s-%d-analysis.events", className, methodId)).toString())
            .build();
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(DeferredOctagonState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .withTelemetry(PicoTelemetryFactory.getTelemetryEngine(telemetryOptions))
            .build();
        Runnable runner = new AnalysisRunner(options);
        runner.run();
        return 0;
    }
}
