package driver.commands;

import abstractinterp.scalar.state.IncZoneState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.IncZoneAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "inczone-numerical",
    mixinStandardHelpOptions = true,
    description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncZoneNumericalCommand extends NumericalAnalysisCommand {

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
            .withStateType(IncZoneState.class)
            .withOrderer(orderer)
            .build();
        Runnable runner = new IncZoneAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
