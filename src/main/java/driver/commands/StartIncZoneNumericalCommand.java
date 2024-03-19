package driver.commands;

import driver.IncZoneAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "inczone-numerical",
    mixinStandardHelpOptions = true,
    description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new IncZoneAnalysisRunner(
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
