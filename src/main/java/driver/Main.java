package driver;

import java.util.concurrent.Callable;

import driver.commands.GenerateOctagonsCommand;
import driver.commands.PrintJimpleCommand;
import driver.commands.Smt2FormatCommand;
import driver.commands.Smt2FormatDirectoryCommand;
import driver.commands.Smt2FormatFullCommand;
import driver.commands.Smt2FormatIdentifiersCommand;
import driver.commands.Smt2GraphsCommand;
import driver.commands.StartClassEnumeratorCommand;
import driver.commands.StartDeferredIncrementalOctagonNumericalCommand;
import driver.commands.StartDeferredOctagonNumericalCommand;
import driver.commands.StartIncZoneNumericalCommand;
import driver.commands.StartIncZoneWithChangePriorityNumericalCommand;
import driver.commands.StartIncrementalOctagonNumericalCommand;
import driver.commands.StartIncrementalZOctagonNumericalCommand;
import driver.commands.StartIntervalNumericalCommand;
import driver.commands.StartMaxZoneNumericalCommand;
import driver.commands.StartMethodStatsCommand;
import driver.commands.StartMinZoneNumericalCommand;
import driver.commands.StartOctagonNumericalCommand;
import driver.commands.StartPredicateNumericalCommand;
import driver.commands.StartZoneNumericalCommand;
import driver.commands.ValueExtractionCommand;
import driver.commands.VariablePredicateCounterCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "dfa-smt",
         mixinStandardHelpOptions = true,
         version = "1.0",
         description = "Run various dfa-smt utilities",
         subcommands = {
             PrintJimpleCommand.class,
             GenerateOctagonsCommand.class,
             StartIntervalNumericalCommand.class,
             StartPredicateNumericalCommand.class,
             StartIncZoneNumericalCommand.class,
             StartIncZoneWithChangePriorityNumericalCommand.class,
             StartZoneNumericalCommand.class,
             StartMinZoneNumericalCommand.class,
             StartMaxZoneNumericalCommand.class,
             StartOctagonNumericalCommand.class,
             StartDeferredOctagonNumericalCommand.class,
             StartIncrementalOctagonNumericalCommand.class,
             StartIncrementalZOctagonNumericalCommand.class,
             StartDeferredIncrementalOctagonNumericalCommand.class,
             StartMethodStatsCommand.class,
             StartClassEnumeratorCommand.class,
             Smt2FormatCommand.class,
             Smt2FormatFullCommand.class,
             Smt2FormatIdentifiersCommand.class,
             Smt2FormatDirectoryCommand.class,
             Smt2GraphsCommand.class,
             ValueExtractionCommand.class,
             VariablePredicateCounterCommand.class,
         })
public class Main implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }

    public static void main(String[] args) {
        Integer exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
