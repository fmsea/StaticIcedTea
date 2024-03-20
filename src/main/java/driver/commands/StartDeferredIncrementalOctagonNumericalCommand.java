package driver.commands;

import abstractinterp.scalar.state.DeferredIncrementalOctagonState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.DeferredIncrementalOctagonAnalysisRunner;
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
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(DeferredIncrementalOctagonState.class)
            .withOrderer(orderer)
            .build();
        Runnable runner = new DeferredIncrementalOctagonAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
