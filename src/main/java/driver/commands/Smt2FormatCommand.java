package driver.commands;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import driver.commands.validation.Smt2FormatTypeConverter;

import processing.Smt2FormatReachable;
import processing.Smt2FormatType;

@Command(name = "smt2-format",
         mixinStandardHelpOptions=true,
         description = "create SMT formula to (assert (=> f1 f2))")
public class Smt2FormatCommand implements Callable<Integer> {

    @Option(names = {"-t", "--type"},
            description = "Type of format to use, either full or min",
            defaultValue = "min",
            converter = Smt2FormatTypeConverter.class)
    private Smt2FormatType type;

    @Parameters(index = "0",
                description="filename of first file")
    private String analysisOne;

    @Parameters(index = "1",
                description="filename of second file")
    private String analysisTwo;

    @Parameters(index = "2",
                description="filename of output smt file")
    private String outputFile;

    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("smt-format");
        try (Reader fh1 = new FileReader(analysisOne);
             Reader fh2 = new FileReader(analysisTwo);
             Writer out = new FileWriter(outputFile)) {
            Smt2FormatReachable.Smt2FormatReachable(fh1, fh2, out, type);
            return 0;
        } catch (IOException ex) {
            log.error("Unable to format analysis: {}", ex.toString());
            log.trace(Stream.of(ex.getStackTrace())
                      .map(StackTraceElement::toString)
                      .collect(Collectors.joining("\n")));
            return 1;
        }
    }
}
