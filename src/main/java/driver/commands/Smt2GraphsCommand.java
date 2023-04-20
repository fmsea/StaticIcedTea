package driver.commands;

import java.io.BufferedWriter;
import java.io.File;
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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.AnalysisSMT2Graphs;

@Command(name = "smt2-graphs",
         mixinStandardHelpOptions=true,
         description = "Output (relational) state graphs for analysis output")
public class Smt2GraphsCommand implements Callable<Integer> {

    @Parameters(index = "0",
                description="filename of first file")
    private String analysis;

    @Parameters(index = "1",
                description="directory prefix of output dot files")
    private Path outputDirectory;

    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("smt2-graphs");
        try (Reader fh1 = new FileReader(analysis)) {
            AnalysisSMT2Graphs.exprsToGraphs(fh1, outputDirectory);
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
