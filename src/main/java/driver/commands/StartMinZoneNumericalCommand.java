package driver.commands;

import driver.MinZoneAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "minzone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Min Zone Numerical Analysis")
public class StartMinZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new MinZoneAnalysisRunner(
            className,
            methodId,
            outputResultsPath);
        runner.run();
        return 0;
    }
}
