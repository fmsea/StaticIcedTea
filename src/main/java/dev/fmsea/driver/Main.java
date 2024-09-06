package dev.fmsea.driver;

import java.util.concurrent.Callable;

import dev.fmsea.driver.commands.GenerateOctagonsCommand;
import dev.fmsea.driver.commands.PrintJimpleCommand;
import dev.fmsea.driver.commands.Smt2FormatCommand;
import dev.fmsea.driver.commands.Smt2FormatDirectoryCommand;
import dev.fmsea.driver.commands.Smt2FormatFullCommand;
import dev.fmsea.driver.commands.Smt2FormatIdentifiersCommand;
import dev.fmsea.driver.commands.Smt2GraphsCommand;
import dev.fmsea.driver.commands.StartClassEnumeratorCommand;
import dev.fmsea.driver.commands.StartDeferredIncrementalOctagonNumericalCommand;
import dev.fmsea.driver.commands.StartDeferredOctagonNumericalCommand;
import dev.fmsea.driver.commands.StartIncZoneNumericalCommand;
import dev.fmsea.driver.commands.StartIncZoneWithChangePriorityNumericalCommand;
import dev.fmsea.driver.commands.StartIncrementalOctagonNumericalCommand;
import dev.fmsea.driver.commands.StartIncrementalZOctagonNumericalCommand;
import dev.fmsea.driver.commands.StartIntervalNumericalCommand;
import dev.fmsea.driver.commands.StartMaxZoneNumericalCommand;
import dev.fmsea.driver.commands.StartMethodStatsCommand;
import dev.fmsea.driver.commands.StartMinZoneNumericalCommand;
import dev.fmsea.driver.commands.StartOctagonNumericalCommand;
import dev.fmsea.driver.commands.StartPredicateNumericalCommand;
import dev.fmsea.driver.commands.StartZoneNumericalCommand;
import dev.fmsea.driver.commands.ValueExtractionCommand;
import dev.fmsea.driver.commands.VariablePredicateCounterCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "sit",
         mixinStandardHelpOptions = true,
         version = "1.0",
         description = "Run various DFA-SMT utilities for IcedTea",
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
