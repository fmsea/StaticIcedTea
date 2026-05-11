package dev.fmsea.driver.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.common.Locals;
import dev.fmsea.processing.Smt2Projection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "smt-projection",
         mixinStandardHelpOptions=true,
         description = "Project SMT output toward two variables")
public class Smt2ProjectionCommand implements Callable<Integer> {

    @Option(names={"--vars"},
            required=true,
            description="Variables to project, should not be more than two")
    private List<String> variables;

    @Parameters(index = "0",
                description="Input analysis file to rewrite")
    private String inputAnalysis;

    @Parameters(index = "1",
                description="Output analysis file")
    private String outputAnalysis;


    @Override
    public Integer call() {
        Logger log = LoggerFactory.getLogger("smt-format");
        File output = Path.of(outputAnalysis).toFile();
        try (Reader fh = new FileReader(inputAnalysis);
             Writer out = new FileWriter(output);
             BufferedWriter buf = new BufferedWriter(out)) {
            Smt2Projection.format(Locals.map(variables), fh, buf);
            buf.flush();
            output.setReadOnly();
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
