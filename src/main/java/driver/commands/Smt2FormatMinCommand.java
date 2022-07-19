package driver.commands;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.Smt2FormatMin;

@Command(name = "smt2-format-min",
         mixinStandardHelpOptions=true,
         description = "create SMT formula to (assert (=> f1 f2))")
public class Smt2FormatMinCommand implements Callable<Integer> {

    @Parameters(description="filename of first full smt output")
    private Path analysisOneFull;

    @Parameters(description="filename of first changed smt output")
    private Path analysisOneChanged;

    @Parameters(description="filename of second full smt output")
    private Path analysisTwoFull;

    @Parameters(description="filename of second changed smt output")
    private Path analysisTwoChanged;

    @Parameters(description="filename of output smt file")
    private String outputFile;

    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("smt-format");
        try (Reader oneFull = new FileReader(analysisOneFull.toFile());
             Reader oneChanged = new FileReader(analysisOneChanged.toFile());
             Reader twoFull = new FileReader(analysisTwoFull.toFile());
             Reader twoChanged = new FileReader(analysisTwoChanged.toFile());
             Writer out = new FileWriter(outputFile)) {
            Smt2FormatMin.Smt2FormatMin(oneFull, oneChanged, twoFull, twoChanged, out);
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
