package driver.commands;

import abstractinterp.scalar.state.ZoneState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.ZoneAnalysisRunner;
import driver.util.SootInitialization;
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
        Runnable runner = new ZoneAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
