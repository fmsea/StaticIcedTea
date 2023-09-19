package driver.commands;

import java.util.Set;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import driver.util.SootInitialization;
import driver.util.OrdererType;
import driver.commands.validation.OrdererTypeConverter;
import driver.OctagonAnalysisRunner;

@Command(name = "octagon-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Octagon Analysis")
public class StartOctagonNumericalCommand implements Callable<Integer> {
    @Option(names = {"-o", "--output"},
            description = "Output file path for results",
            required = true)
    private Path outputResultsPath;

    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath to bytecode to analyze",
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

    @Option(names = {"--order"},
            description = "CFG Topological Orderer",
            defaultValue = "pseudo",
            converter = OrdererTypeConverter.class)
    private OrdererType orderer;

    @Parameters(index = "0",
                description = "Class name of artifact to analyze")
    private String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    private int methodId;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner;
        runner = new OctagonAnalysisRunner(className,
                                           methodId,
                                           outputResultsPath,
                                           outputReport,
                                           widenIterations,
                                           widenSteps,
                                           orderer);
        runner.run();
        return 0;
    }
}
