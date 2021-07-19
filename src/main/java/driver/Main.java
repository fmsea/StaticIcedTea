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
             StartIntervalNumericalCommand.class,
             StartDisjointAnalysisCommand.class,
             StartDBSNumericalCommand.class,
             Smt2FormatCommand.class,
             Smt2FormatDirectoryCommand.class,
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
