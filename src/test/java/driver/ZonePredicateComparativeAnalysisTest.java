package driver;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import util.Compiler;

import driver.providers.ZonesPredicateComparativeProvider;

public class ZonePredicateComparativeAnalysisTest extends NumericalAnalysisTest {

    @ParameterizedTest
    @ArgumentsSource(ZonesPredicateComparativeProvider.class)
    void test(String name,
              String source,
              String dom1ExpectedChanged,
              String dom1ExpectedFull,
              String dom2ExpectedChanged,
              String dom2ExpectedFull,
              String expectedFormula,
              String expectedResults) throws Exception {
        Path dom1OutputDir = Paths.get(this.testOutputDir.toString(), "/zone-analysis/");
        Path dom2OutputDir = Paths.get(this.testOutputDir.toString(), "/predicate-dom3-sY/");
        dom1OutputDir.toFile().mkdirs();
        dom2OutputDir.toFile().mkdirs();
        Path clazz = Compiler.compileSource(name, source);
        Process analysis1 = Runtime.getRuntime().exec(new String[] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "zone-numerical",
                "--classpath",
                clazz.getParent().toString(),
                "--output",
                dom1OutputDir.toString(),
                name,
                "1",
            });
        Process analysis2 = Runtime.getRuntime().exec(new String[] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "predicate",
                "--classpath",
                clazz.getParent().toString(),
                "--domain",
                "analysis/ExperimentData/domains/dom3.txt",
                "--output",
                dom2OutputDir.toString(),
                "--symbolic",
                "Y",
                name,
                "1",
            });
        analysis1.waitFor(60l, TimeUnit.SECONDS);
        analysis2.waitFor(60l, TimeUnit.SECONDS);
        Path dom1ActualChangedOutput = Paths.get(dom1OutputDir.toString(),
                                                 String.format("%s_1.changed.out", name));
        Path dom1ActualFullSmtOutput = Paths.get(dom1OutputDir.toString(),
                                                 String.format("%s_1.smt.out", name));
        Path dom2ActualChangedOutput = Paths.get(dom2OutputDir.toString(),
                                                 String.format("%s_1.changed.out", name));
        Path dom2ActualFullSmtOutput = Paths.get(dom2OutputDir.toString(),
                                                 String.format("%s_1.smt.out", name));
        Path actualFormulaOutput = Paths.get(this.testOutputDir.toString(),
                                             String.format("zone-analysis-predicate-dom3-sY.%s_1.smt", name));
        Process prepareFormula = Runtime.getRuntime().exec(new String[] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.Main",
                "smt2-format-min",
                dom1ActualFullSmtOutput.toString(),
                dom1ActualChangedOutput.toString(),
                dom2ActualFullSmtOutput.toString(),
                dom2ActualChangedOutput.toString(),
                actualFormulaOutput.toString(),
            });
        prepareFormula.waitFor(60l, TimeUnit.SECONDS);
        Process z3 = Runtime.getRuntime().exec(new String[] {
                "z3",
                "-smt2",
                actualFormulaOutput.toString(),
            });
        z3.waitFor(60l, TimeUnit.SECONDS);
        String actualResults = new BufferedReader(new InputStreamReader(z3.getInputStream(),
                                                                        StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n")).trim();
        assertAll(() -> assertEquals(Optional.of(dom1ExpectedChanged), readFile(dom1ActualChangedOutput)),
                  () -> assertEquals(Optional.of(dom1ExpectedFull), readFile(dom1ActualFullSmtOutput)),
                  () -> assertEquals(Optional.of(dom2ExpectedChanged), readFile(dom2ActualChangedOutput)),
                  () -> assertEquals(Optional.of(dom2ExpectedFull), readFile(dom2ActualFullSmtOutput)),
                  () -> assertEquals(Optional.of(expectedFormula), readFile(actualFormulaOutput)),
                  () -> assertEquals(expectedResults, actualResults));
    }
}
