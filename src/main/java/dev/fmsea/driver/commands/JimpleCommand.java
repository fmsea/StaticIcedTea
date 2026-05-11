package dev.fmsea.driver.commands;

import java.nio.file.Path;

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
