package driver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
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

public class IncZoneNumericalAnalysisTest extends NumericalAnalysisTest {

    @ParameterizedTest
    @ArgumentsSource(ZonesMiniJavaExamplesProvider.class)
    void testAnalysis(String name,
                      String source,
                      String expectedFullSmtOutput) throws Exception {
        Path clazz = Compiler.compileSource(name, source);
        ProcessBuilder pb = new ProcessBuilder(new String [] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "inczone-numerical",
                "--classpath",
                clazz.getParent().toString(),
                "--output",
                this.testOutputDir.toString(),
                name,
                "1",
            });
        Map<String, String> envVars = pb.environment();
        envVars.put("DFA_EXPORT_GRAPH_STATES", "true");
        Process analysis = pb.start();
        analysis.waitFor(60l, TimeUnit.SECONDS);
        try {
            Path fullSmtOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.smt.out", name));
            String fullSmtOutput = Files.readString(fullSmtOutputPath);
            String error = new BufferedReader(new InputStreamReader(analysis.getErrorStream()))
                .lines().collect(Collectors.joining("\n"));
            assertEquals(expectedFullSmtOutput, fullSmtOutput.trim(), "Full Report Not Equal");
        } catch (IOException ex) {
            System.err.println("Unable to assert interval analysis");
            System.err.println(ex.getMessage());
            String error = new BufferedReader(new InputStreamReader(analysis.getErrorStream()))
                .lines().collect(Collectors.joining("\n"));
            System.err.println(error);
            assertTrue(false);
        }
    }
}
