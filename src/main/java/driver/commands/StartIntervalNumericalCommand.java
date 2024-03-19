package driver.commands;

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
        Runnable runner = new IntervalAnalysisRunner(
            className,
            methodId,
            outputResultsPath,
            outputReport,
            widenIterations,
            widenSteps);
        runner.run();
        return 0;
    }
}
