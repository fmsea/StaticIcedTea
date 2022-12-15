package driver.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import driver.ValueExtractionAnalysisRunner;
import driver.util.SootInitialization;

@Command(name = "value-extraction",
         mixinStandardHelpOptions = true,
         description = "Extract predicates from provided method")
public class ValueExtractionCommand implements Callable<Integer> {
    @Option(names = {"-o", "--output"},
            description = "OUtput file path for results",
            required = true)
    private Path outputResultsPath;

    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath to bytecode to analyze",
            required = true)
    private Path classpath;

    @Parameters(index = "0",
                description = "(fully qualified) Class name of method to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new ValueExtractionAnalysisRunner(className,
                                                            methodId,
                                                            outputResultsPath);
        runner.run();
        return 0;
    }
}
