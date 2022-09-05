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

import driver.providers.PredicateMiniJavaExamplesProvider;

public class PredicateNumericalAnalysisTest extends NumericalAnalysisTest {

    @ParameterizedTest
    @ArgumentsSource(PredicateMiniJavaExamplesProvider.class)
    void testPredicateNumericalAnalysis(String name,
                                        String source,
                                        String expectedChangedOutput,
                                        String expectedSubgraphOutput,
                                        String expectedMinSubgraphOutput,
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
            Path changedOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.changed.out", name));
            Path subgraphOutputPath = Paths.get(this.testOutputDir.toString(),
                                                String.format("%s_1.subgraph.out", name));
            Path minSubgraphOutputPath = Paths.get(this.testOutputDir.toString(),
                                                   String.format("%s_1.subgraph-min.out", name));
            Path fullSmtOutputPath = Paths.get(this.testOutputDir.toString(),
                                               String.format("%s_1.smt.out", name));
            String changedOutput = Files.readString(changedOutputPath);
            String subgraphOutput = Files.readString(subgraphOutputPath);
            String minSubgraphOutput = Files.readString(minSubgraphOutputPath);
            String fullSmtOutput = Files.readString(fullSmtOutputPath);
            assertAll(() -> assertEquals(expectedChangedOutput, changedOutput.trim(), "Changed Report Not Equal"),
                      () -> assertEquals(expectedSubgraphOutput, subgraphOutput.trim(), "Subgraph Report Not Equal"),
                      () -> assertEquals(expectedMinSubgraphOutput, minSubgraphOutput.trim(), "Minimum Subgraph Report Not Equal"),
                      () -> assertEquals(expectedFullSmtOutput, fullSmtOutput.trim(), "Full Report Not Equal"));
        } catch (IOException ex) {
            System.err.println("Unable to assert analysis");
            System.err.println(ex.getMessage());
            assertTrue(false);
        }
    }
}
