package driver.commands;

import abstractinterp.scalar.state.MinZoneState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.MinZoneAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "minzone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Min Zone Numerical Analysis")
public class StartMinZoneNumericalCommand extends NumericalAnalysisCommand {

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
            .withStateType(MinZoneState.class)
            .withOrderer(orderer)
            .build();
        Runnable runner = new MinZoneAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
