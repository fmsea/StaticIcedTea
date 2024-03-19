package driver.commands;

import driver.IncrementalOctagonAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;
import util.Properties;

@Command(name = "incoct-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Incremental Octagon Analysis using Search")
public class StartIncrementalOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Properties.IncrementalClosureAlgorithm = Properties.OctagonIncrementalClosureAlgorithm.SEARCH;
        Runnable runner = new IncrementalOctagonAnalysisRunner(
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
