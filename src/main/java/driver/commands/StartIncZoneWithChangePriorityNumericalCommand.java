package driver.commands;

import abstractinterp.scalar.state.IncZoneStateWithChangePriority;
import abstractinterp.scalar.state.factory.IncZoneStateWithChangePriorityFactory;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.IncZoneWithChangePriorityAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "inczone-numerical-with-delta-p",
         mixinStandardHelpOptions = true,
         description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncZoneWithChangePriorityNumericalCommand extends NumericalAnalysisCommand {

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
            .withStateType(IncZoneStateWithChangePriority.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new IncZoneWithChangePriorityAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
