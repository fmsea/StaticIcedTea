package dev.fmsea.driver.commands;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.fmsea.driver.ValueExtractionAnalysisRunner;
import dev.fmsea.driver.util.SootInitialization;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "value-extraction",
         mixinStandardHelpOptions = true,
         description = "Extract predicates from provided method")
public class ValueExtractionCommand extends JimpleCommand implements Callable<Integer> {
    @Option(names = {"-o", "--output"},
            description = "OUtput file path for results",
            required = true)
    private Path outputResultsPath;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new ValueExtractionAnalysisRunner(
            className,
            methodId,
            outputResultsPath);
        runner.run();
        return 0;
    }
}
