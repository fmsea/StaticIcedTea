package abstractinterp.scalar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.Smt2FormatReachable;

public abstract class AbstractNumericalTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractNumericalTest.class);

    protected String generateReport(Analysis analysis) {
        try (StringWriter writer = new StringWriter()) {
            analysis.writeReport(writer);
            writer.flush();
            return writer.toString();
        } catch (IOException ex) {
            LOGGER.error("IO error during report generation: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            return "";
        }
    }

    protected String readResourcesFile(String fileName) {
        try {
            String resourceFileName = "abstractinterp/scalar/" + fileName;
            ClassLoader loader = getClass().getClassLoader();
            Path resourceFile = Paths.get(loader.getResource(resourceFileName).getFile());
            return Files.readString(resourceFile).trim();
        } catch (IOException ex) {
            LOGGER.error("exception while reading test resource file: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            LOGGER.error("unknown error: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            throw new RuntimeException(ex);
        }
    }

    protected boolean runComparison(String left, String right, String oracle, Path z3TestFile) {
        Reader leftReader = new StringReader(left);
        Reader rightReader = new StringReader(right);
        try {
            Writer writer = new FileWriter(z3TestFile.toFile());
            Smt2FormatReachable.Smt2FormatReachable(leftReader, rightReader, writer);
            Process z3 = Runtime.getRuntime().exec(new String[] {
                "z3",
                "-smt2",
                z3TestFile.toString(),
            });
            z3.waitFor(60l, TimeUnit.SECONDS);
            String output = new BufferedReader(new InputStreamReader(z3.getInputStream(),
                                                                     StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            assertEquals(oracle, output);
            return true;
        } catch (IOException ex) {
            LOGGER.error("IO error while performing comparison: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            return false;
        } catch (InterruptedException ex) {
            LOGGER.error("comparison interrupted: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            return false;
        } catch (Exception ex) {
            LOGGER.error("unknown error: {}", ex);
            LOGGER.error(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
            return false;
        }
    }
}
