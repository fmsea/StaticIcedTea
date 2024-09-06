package dev.fmsea.driver.commands;

import java.util.concurrent.Callable;

import dev.fmsea.driver.PrintJimpleRunner;
import dev.fmsea.driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "print-jimple",
         mixinStandardHelpOptions = true,
         description = "Compute statements counts and data about a method")
public class PrintJimpleCommand extends JimpleCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new PrintJimpleRunner(className, methodId);
        runner.run();
        return 0;
    }
}
