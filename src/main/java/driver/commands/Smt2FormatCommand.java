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
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.Smt2Format;

@Command(name = "smt2-format",
         mixinStandardHelpOptions=true,
         description = "create SMT formula to (assert (=> f1 f2))")
public class Smt2FormatCommand implements Callable<Integer> {

    @Parameters(description="filename of first file")
    private String analysisOne;

    @Parameters(description="filename of second file")
    private String analysisTwo;

    @Parameters(description="filename of output smt file")
    private String outputFile;

    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("smt-format");
        try (Reader fh1 = new FileReader(analysisOne);
             Reader fh2 = new FileReader(analysisTwo);
             Writer out = new FileWriter(outputFile)) {
            Smt2Format.SMT2Format(fh1, fh2, out);
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
