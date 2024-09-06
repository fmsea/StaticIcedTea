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

import dev.fmsea.driver.commands.validation.Smt2FormatTypeConverter;
import dev.fmsea.driver.commands.validation.Smt2LogicConverter;
import dev.fmsea.driver.commands.validation.Smt2UnionTypeConverter;

import dev.fmsea.processing.Smt2FormatReachable;
import dev.fmsea.processing.Smt2FormatType;
import dev.fmsea.processing.Smt2UnionType;
import dev.fmsea.solver.Smt2Logic;

@Command(name = "smt2-format",
         mixinStandardHelpOptions=true,
         description = "create SMT formula to (assert (=> f1 f2))")
public class Smt2FormatCommand implements Callable<Integer> {

    @Option(names = {"-t", "--type"},
            description = "Type of format to use, either full or min",
            defaultValue = "min",
            converter = Smt2FormatTypeConverter.class)
    private Smt2FormatType type;

    @Option(names = {"--logic"},
            description = "SMT Logic to set at the top of the entailed file",
            defaultValue = "LIA",
            converter = Smt2LogicConverter.class)
    private Smt2Logic logic;

    @Option(names = {"--union-method"},
            description = "The fixed-point method for selecting variables from expressions, either CONNECTED or REACHABLE",
            defaultValue = "reachable",
            converter = Smt2UnionTypeConverter.class)
    private Smt2UnionType unionType;

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
        File output = Path.of(outputFile).toFile();
        try (Reader fh1 = new FileReader(analysisOne);
             Reader fh2 = new FileReader(analysisTwo);
             Writer out = new FileWriter(output);
             BufferedWriter buf = new BufferedWriter(out)) {
            Smt2FormatReachable.Smt2FormatReachable(fh1, fh2, buf, type, logic, unionType);
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
