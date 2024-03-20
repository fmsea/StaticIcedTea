package driver.commands;

import abstractinterp.scalar.state.IntervalBoxState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.IntervalAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "interval-numerical",
         mixinStandardHelpOptions = true,
         description = "Run IntervalNumerical Analysis")
public class StartIntervalNumericalCommand extends NumericalAnalysisCommand {

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
            .withStateType(IntervalBoxState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new IntervalAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
