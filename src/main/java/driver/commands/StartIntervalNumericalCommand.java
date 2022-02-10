package driver.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import soot.SootMethod;
import soot.Body;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import driver.IntervalAnalysisRunner;
import driver.util.SootInitialization;



@Command(name = "interval-numerical",
         mixinStandardHelpOptions = true,
         description = "Run IntervalNumerical Analysis")
public class StartIntervalNumericalCommand implements Callable<Integer> {

    @Option(names = {"-o", "--output"},
            description = "Output file path for results",
            required = false)
    private Path outputResultsPath;

    @Option(names = {"-cp", "--classpath"},
            description = "Classpath of bytecode to analyze",
            required = true)
    private Path classpath;

    @Option(names = {"--full-report"},
            description = "Print the entire state for each unit",
            required = false)
    private boolean fullReport;

    @Parameters(index = "0",
                description = "Class Name of artifact to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new IntervalAnalysisRunner(className,
                                                     methodId,
                                                     outputResultsPath,
                                                     fullReport);
        runner.run();
        return 0;
    }
}
