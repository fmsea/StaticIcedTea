package driver.commands;

import driver.MaxAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "maxzone-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Max Zone Numerical Analysis")
public class StartMaxZoneNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new MaxAnalysisRunner(className,
                                                methodId,
                                                outputResultsPath);
        runner.run();
        return 0;
    }
}
