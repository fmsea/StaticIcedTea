package driver.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import driver.util.SootInitialization;
import driver.PrintJimpleRunner;

@Command(name = "print-jimple",
         mixinStandardHelpOptions = true,
         description = "Compute statements counts and data about a method")
public class PrintJimpleCommand implements Callable<Integer> {
    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath to bytecode to analyze",
            required = true)
    private Path classpath;

    @Parameters(index = "0",
                description = "Class name of artifact to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new PrintJimpleRunner(className, methodId);
        runner.run();
        return 0;
    }
}
