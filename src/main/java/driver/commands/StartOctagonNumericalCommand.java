package driver.commands;

import driver.OctagonAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "octagon-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Octagon Analysis")
public class StartOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new OctagonAnalysisRunner(
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
