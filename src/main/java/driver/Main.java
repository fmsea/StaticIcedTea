package driver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import driver.commands.*;

@Command(name = "dfa-smt",
         mixinStandardHelpOptions = true,
         version = "1.0",
         description = "Run various dfa-smt utilities",
         subcommands = {
             PrintJimpleCommand.class,
             StartIntervalNumericalCommand.class,
             StartPredicateNumericalCommand.class,
             StartIncZoneNumericalCommand.class,
             StartZoneNumericalCommand.class,
             StartMinZoneNumericalCommand.class,
             StartMaxZoneNumericalCommand.class,
             StartMethodStatsCommand.class,
             Smt2FormatCommand.class,
             Smt2FormatFullCommand.class,
             Smt2FormatIdentifiersCommand.class,
             Smt2FormatDirectoryCommand.class,
             Smt2FormatMinCommand.class,
             Smt2FormatMinReachableCommand.class,
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
