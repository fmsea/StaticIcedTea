package driver.commands;

import java.util.Set;

import driver.commands.validation.OrdererTypeConverter;
import driver.util.OrdererType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public abstract class NumericalAnalysisCommand extends AnalysisCommand {

    @Option(names = {"--widen-after", "-k"},
            description = "Widen widening nodes after `k` iterations",
            required = false,
            defaultValue = "2")
    protected int widenIterations;

    @Option(names = {"--widen-steps", "-S"},
            description = "Use step values for widening",
            required = false)
    protected Set<Integer> widenSteps;

    @Option(names = {"--order"},
            description = "CFG Topological Orderer",
            defaultValue = "pseudo",
            converter = OrdererTypeConverter.class)
    protected OrdererType orderer;

    public abstract Integer call() throws Exception;
}
