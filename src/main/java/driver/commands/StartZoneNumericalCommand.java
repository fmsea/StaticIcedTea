package driver.commands;

import driver.ZoneAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "zone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Zone Numerical Analysis")
public class StartZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new ZoneAnalysisRunner(
            className,
            methodId,
            outputResultsPath);
        runner.run();
        return 0;
    }
}
