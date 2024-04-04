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

    @Option(names = {"--reduce"},
        description = "Reduce each output state before printing",
        required = false,
        defaultValue = "true",
        fallbackValue = "true",
        negatable = true)
    protected boolean reduceOutput;

    @Option(names = {"--min-delta-vars"},
        description = "Output minimum changed variables, can be negated",
        required = false,
        defaultValue = "true",
        fallbackValue = "true",
        negatable = true)
    protected boolean outputMinimum;

    public abstract Integer call() throws Exception;
}
