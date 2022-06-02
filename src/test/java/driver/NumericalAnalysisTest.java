package driver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.File;
import java.io.StringReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class NumericalAnalysisTest {

    protected Path testOutputDir;

    @BeforeEach
    protected void setup() {
        try {
            this.testOutputDir = Files.createTempDirectory(String.format("dfa-smt-%s",
                                                                         String.valueOf(System.nanoTime())));
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @AfterEach
    protected void teardown() {
        try (Stream<Path> dirStream = Files.walk(this.testOutputDir)) {
            dirStream
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(File::delete);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
