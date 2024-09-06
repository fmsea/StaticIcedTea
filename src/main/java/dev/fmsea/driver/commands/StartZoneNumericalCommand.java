package dev.fmsea.driver.commands;

import dev.fmsea.absint.scalar.state.ZoneState;
import dev.fmsea.driver.AnalysisOptions;
import dev.fmsea.driver.AnalysisOptionsBuilder;
import dev.fmsea.driver.AnalysisRunner;
import dev.fmsea.driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "zone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Zone Numerical Analysis")
public class StartZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(ZoneState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new AnalysisRunner(options);
        runner.run();
        return 0;
    }
}
