package driver.commands;

import abstractinterp.scalar.state.MaxZoneState;
import abstractinterp.scalar.state.factory.MaxZoneStateFactory;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.MaxAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "maxzone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Max Zone Numerical Analysis")
public class StartMaxZoneNumericalCommand extends NumericalAnalysisCommand {

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
            .withStateType(MaxZoneState.class)
            .withOrderer(orderer)
            .build();
        Runnable runner = new MaxAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
