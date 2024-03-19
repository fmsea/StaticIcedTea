package driver.commands;

import driver.IncZoneWithChangePriorityAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "inczone-numerical-with-delta-p",
         mixinStandardHelpOptions = true,
         description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncZoneWithChangePriorityNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new IncZoneWithChangePriorityAnalysisRunner(
            className,
            methodId,
            outputResultsPath,
            outputReport,
            widenIterations);
        runner.run();
        return 0;
    }
}
