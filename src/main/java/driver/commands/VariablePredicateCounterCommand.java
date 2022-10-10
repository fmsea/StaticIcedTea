package driver.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.AnalysisSMTReport;
import processing.Smt2Reader;
import processing.VariablePredicateStats;

@Command(name = "count-variable-predicates",
         mixinStandardHelpOptions = true,
         description = "Count variables and predicates in SMT result files.")
public class VariablePredicateCounterCommand implements Callable<Integer> {

    @Parameters(description="filename of SMT output to process")
    private Path smtFile;

    @Parameters(description="filename of resulting JSON file")
    private String outputFile;

    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("count-variable-predicates");
        try (Reader reader = new FileReader(this.smtFile.toFile());
             BufferedReader bufReader = new BufferedReader(reader);
             Writer writer = new FileWriter(this.outputFile);
             BufferedWriter bufWriter = new BufferedWriter(writer)) {
            AnalysisSMTReport report = Smt2Reader.parse(bufReader);
            VariablePredicateStats stats = VariablePredicateStats.from(report);
            bufWriter.write(stats.toJSON());
            return 0;
        } catch (IOException ex) {
            log.error("Unable to process SMT file, {}", ex.getMessage());
            log.trace(Stream.of(ex.getStackTrace())
                      .map(StackTraceElement::toString)
                      .collect(Collectors.joining("\n")));
            return -1;
        }
    }
}
