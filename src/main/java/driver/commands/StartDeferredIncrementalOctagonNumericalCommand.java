package driver.commands;

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
        Properties.IncrementalClosureAlgorithm = Properties.IncrementalClosureAlgorithm.SEARCH;
        Runnable runner = new DeferredIncrementalOctagonAnalysisRunner(
            className,
            methodId,
            outputResultsPath,
            outputReport,
            widenIterations,
            widenSteps,
            orderer);
        runner.run();
        return 0;
    }
}
