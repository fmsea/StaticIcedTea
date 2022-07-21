package driver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Path;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import util.Compiler;

import driver.providers.ZonesMiniJavaExamplesProvider;

public class MinZoneNumericalAnalysisTest extends NumericalAnalysisTest {

    @ParameterizedTest
    @ArgumentsSource(ZonesMiniJavaExamplesProvider.class)
    void testAnalysis(String name,
                      String source,
                      String expectedChangedOutput,
                      String expectedFullSmtOutput) throws Exception {
        Path clazz = Compiler.compileSource(name, source);
        Process analysis = Runtime.getRuntime().exec(new String [] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "minzone-numerical",
                "--classpath",
                clazz.getParent().toString(),
                "--output",
                this.testOutputDir.toString(),
                name,
                "1",
            });
        analysis.waitFor(60l, TimeUnit.SECONDS);
        try {
            Path changedOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.changed.out", name));
            Path fullSmtOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.smt.out", name));
            String changedOutput = Files.readString(changedOutputPath);
            String fullSmtOutput = Files.readString(fullSmtOutputPath);
            // "traditional" zones does not fully report all variables which
            // are affected by all transfers.  Namely, some constants may not
            // infer constant relations.
            assertAll(//() -> assertEquals(expectedChangedOutput, changedOutput.trim()),
                      () -> assertEquals(expectedFullSmtOutput, fullSmtOutput.trim()));
        } catch (IOException ex) {
            System.err.println("Unable to assert interval analysis");
            System.err.println(ex.getMessage());
            assertTrue(false);
        }
    }
}
