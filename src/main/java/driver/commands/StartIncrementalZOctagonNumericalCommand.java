package driver.commands;

import abstractinterp.scalar.state.IncrementalOctagonState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.IncrementalOctagonAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;
import util.Properties;

@Command(name = "incoctz-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Incremental Octagon Analysis using Chawdhary Incremental Closure")
public class StartIncrementalZOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Properties.IncrementalClosureAlgorithm = Properties.OctagonIncrementalClosureAlgorithm.CHAWDHARY;
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(IncrementalOctagonState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new IncrementalOctagonAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
