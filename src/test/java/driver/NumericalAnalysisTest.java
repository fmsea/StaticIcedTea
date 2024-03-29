package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
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
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    protected Optional<String> readFile(Path path) {
        try (Reader fileReader = new FileReader(path.toFile());
             BufferedReader reader = new BufferedReader(fileReader)) {
            return Optional.of(reader.lines().collect(Collectors.joining("\n")).trim());
        } catch (IOException ex) {
            System.err.println("Error reading output file for test");
            System.err.println(ex.toString());
            ex.printStackTrace(System.err);
        }
        return Optional.empty();
    }
}
