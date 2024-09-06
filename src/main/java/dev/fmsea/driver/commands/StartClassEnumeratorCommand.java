package dev.fmsea.driver.commands;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.fmsea.driver.ClassEnumeratorRunner;
import dev.fmsea.driver.util.SootInitialization;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "class-enumerator",
         mixinStandardHelpOptions = true,
         description = "Compute statements counts and data about a method")
public class StartClassEnumeratorCommand implements Callable<Integer> {
    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath to bytecode to analyze",
            required = true)
    private Path classpath;

    @Parameters(index = "0",
                description = "Class name of artifact to analyze")
    private String className;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new ClassEnumeratorRunner(className);
        runner.run();
        return 0;
    }
}
