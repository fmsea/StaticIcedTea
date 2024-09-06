package dev.fmsea.driver.commands;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public abstract class AnalysisCommand extends JimpleCommand implements Callable<Integer> {

    @Option(names = {"-o", "--output"},
            description = "Output file path for results",
            required = true)
    protected Path outputResultsPath;

    @Option(names = "--report",
            description = "Whether to output state reports",
            required = false,
            defaultValue = "true",
            fallbackValue = "true",
            negatable = true)
    protected boolean outputReport;

    public abstract Integer call() throws Exception;
}
