package dev.fmsea.driver.commands;

import java.util.Set;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.fmsea.driver.commands.validation.OrdererTypeConverter;
import dev.fmsea.driver.util.OrdererType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command
public abstract class JimpleCommand {

    @Option(names = {"-cp", "--classpath", "--class-path"},
            description = "Classpath to bytecode to analyze",
            required = true)
    protected Path classpath;

    @Parameters(index = "0",
                description = "Class name of artifact to analyze")
    protected String className;

    @Parameters(index = "1",
                description = "Method ID to analyze")
    protected int methodId;
}
