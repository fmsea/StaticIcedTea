package driver.commands;

import abstractinterp.scalar.state.IncZoneState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.AnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;
import util.Properties;

@Command(name = "inczone-numerical",
    mixinStandardHelpOptions = true,
    description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Properties.OutputMinimumChangedVariables = outputMinimum;
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(IncZoneState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new AnalysisRunner(options);
        runner.run();
        return 0;
    }
}
