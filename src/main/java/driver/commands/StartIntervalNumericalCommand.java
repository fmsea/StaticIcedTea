package driver.commands;

import java.util.Set;
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
            required = true)
    private Path outputResultsPath;

    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath of bytecode to analyze",
            required = true)
    private Path classpath;

    @Option(names = "--report",
            description = "Whether to output state reports",
            required = false,
            defaultValue = "true",
            fallbackValue = "true",
            negatable = true)
    private boolean outputReport;

    @Option(names = {"--widen-after", "-k"},
            description = "Widen widening nodes after `k` iterations",
            required = false,
            defaultValue = "2")
    private int widenIterations;

    @Option(names = {"--widen-steps", "-S"},
            description = "Use step values for widening",
            required = false)
    private Set<Integer> widenSteps;

    @Parameters(index = "0",
                description = "Class Name of artifact to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner;
        if (widenSteps != null) {
            runner = new IntervalAnalysisRunner(className,
                                                methodId,
                                                outputResultsPath,
                                                outputReport,
                                                widenIterations,
                                                widenSteps);
        } else {
            runner = new IntervalAnalysisRunner(className,
                                                methodId,
                                                outputResultsPath,
                                                outputReport,
                                                widenIterations);
        }
        runner.run();
        return 0;
    }
}
