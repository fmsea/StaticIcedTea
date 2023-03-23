package driver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

import driver.providers.PredicateMiniJavaExamplesProvider;

public class PredicateNumericalAnalysisTest extends NumericalAnalysisTest {

    @ParameterizedTest
    @ArgumentsSource(PredicateMiniJavaExamplesProvider.class)
    void testPredicateNumericalAnalysis(String name,
                                        String source,
                                        String expectedFullSmtOutput) throws Exception {
        Path clazz = Compiler.compileSource(name, source);

        Process analysis = Runtime.getRuntime().exec(new String[] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "predicate",
                "--classpath",
                clazz.getParent().toString(),
                "--output",
                this.testOutputDir.toString(),
                name,
                "1",
                "--domain",
                "analysis/ExperimentData/domains/dom3.txt",
                "--symbolic",
                "Y",
            });
        analysis.waitFor(60l, TimeUnit.SECONDS);
        try {
            Path fullSmtOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.smt.out", name));
            String fullSmtOutput = Files.readString(fullSmtOutputPath);
            assertEquals(expectedFullSmtOutput, fullSmtOutput.trim(), "Full Report Not Equal");
        } catch (IOException ex) {
            System.err.println("Unable to assert analysis");
            System.err.println(ex.getMessage());
            String error = new BufferedReader(new InputStreamReader(analysis.getErrorStream()))
                .lines().collect(Collectors.joining("\n"));
            System.err.println(error);
            assertTrue(false);
        }
    }
}
