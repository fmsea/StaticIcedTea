package driver.commands;

import java.util.concurrent.Callable;

import driver.MethodStatsRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "method-stats",
         mixinStandardHelpOptions = true,
         description = "Compute statements counts and data about a method")
public class StartMethodStatsCommand extends JimpleCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new MethodStatsRunner(className, methodId);
        runner.run();
        return 0;
    }
}
