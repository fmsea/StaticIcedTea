package dev.fmsea.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.driver.providers.PredicateMiniJavaExamplesProvider;
import dev.fmsea.util.Compiler;

public class PredicateNumericalAnalysisTest extends NumericalAnalysisTest {

    protected Path clazz;
    protected File dom3File;

    @BeforeEach
    protected void setup() {
        super.setup();
        this.dom3File = Path.of(this.testOutputDir.toString(), "dom3.txt").toFile();
        try (FileWriter fw = new FileWriter(this.dom3File);
             BufferedWriter buf = new BufferedWriter(fw)) {
            buf.write("(inf,0) 0  (0,inf)");
            buf.write("\n");
            buf.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @AfterEach
    protected void teardown() {
        super.teardown();
        try (Stream<Path> dirStream = Files.walk(this.clazz.getParent())) {
            dirStream
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(PredicateMiniJavaExamplesProvider.class)
    void testPredicateNumericalAnalysis(String name,
        String source,
        String expectedFullSmtOutput) throws Exception {
        clazz = Compiler.compileSource(name, source);

        Process analysis = Runtime.getRuntime().exec(new String[] {
            "java",
            "-classpath",
            System.getProperty("java.class.path"),
            "dev.fmsea.driver.Main",
            "predicate",
            "--classpath",
            clazz.getParent().toString(),
            "--output",
            this.testOutputDir.toString(),
            name,
            "1",
            "--domain",
            dom3File.toString(),
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
