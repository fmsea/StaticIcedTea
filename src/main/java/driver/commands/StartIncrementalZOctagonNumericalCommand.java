package driver.commands;

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
