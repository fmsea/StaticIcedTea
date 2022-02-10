package driver.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import driver.util.SootInitialization;
import driver.IncDBSAnalysisRunner;

@Command(name = "incdbs-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Inc Difference Bounded Numerical Analysis")
public class StartIncDBSNumericalCommand implements Callable<Integer> {
    @Option(names = {"-o", "--output"},
            description = "Output file path for results",
            required = false)
    private Path outputResultsPath;

    @Option(names = {"-cp", "--classpath"},
            description = "Classpath to bytecode to analyze",
            required = true)
    private Path classpath;

    @Option(names = {"--full-report"},
            description = "print the entire state for each program unit",
            required = false)
    private boolean fullReport;

    @Parameters(index = "0",
                description = "Class name of artifact to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new IncDBSAnalysisRunner(className,
                                                   methodId,
                                                   outputResultsPath,
                                                   fullReport);
        runner.run();
        return 0;
    }
}
