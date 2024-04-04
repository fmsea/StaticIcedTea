package driver.commands;

import abstractinterp.scalar.state.DeferredIncrementalOctagonState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.AnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;
import util.Properties;

@Command(name = "defoct-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Deferred Incremental Octagon Analysis")
public class StartDeferredIncrementalOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Properties.IncrementalClosureAlgorithm = Properties.OctagonIncrementalClosureAlgorithm.SEARCH;
        Properties.OutputMinimumChangedVariables = outputMinimum;
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(DeferredIncrementalOctagonState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new AnalysisRunner(options);
        runner.run();
        return 0;
    }
}
