package dev.fmsea.driver;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.driver.providers.ZonesPredicateComparativeProvider;
import dev.fmsea.util.Compiler;

public class ZonePredicateComparativeAnalysisTest extends NumericalAnalysisTest {

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
    @ArgumentsSource(ZonesPredicateComparativeProvider.class)
    void test(String name,
        String source,
        String dom1ExpectedFull,
        String dom2ExpectedFull,
        String expectedFormula,
        String expectedResults) throws Exception {
        Path dom1OutputDir = Paths.get(this.testOutputDir.toString(), "/zone-analysis/");
        Path dom2OutputDir = Paths.get(this.testOutputDir.toString(), "/predicate-dom3-sY/");
        dom1OutputDir.toFile().mkdirs();
        dom2OutputDir.toFile().mkdirs();
        clazz = Compiler.compileSource(name, source);
        Process analysis1 = Runtime.getRuntime().exec(new String[] {
            "java",
            "-classpath",
            System.getProperty("java.class.path"),
            "dev.fmsea.driver.Main",
            "inczone-numerical",
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
            "dev.fmsea.driver.Main",
            "predicate",
            "--classpath",
            clazz.getParent().toString(),
            "--domain",
            this.dom3File.toString(),
            "--output",
            dom2OutputDir.toString(),
            "--symbolic",
            "Y",
            name,
            "1",
        });
        analysis1.waitFor(60l, TimeUnit.SECONDS);
        analysis2.waitFor(60l, TimeUnit.SECONDS);
        Path dom1ActualFullSmtOutput = Paths.get(dom1OutputDir.toString(),
            String.format("%s_1.smt.out", name));
        Path dom2ActualFullSmtOutput = Paths.get(dom2OutputDir.toString(),
            String.format("%s_1.smt.out", name));
        Path actualFormulaOutput = Paths.get(this.testOutputDir.toString(),
            String.format("zone-analysis-predicate-dom3-sY.%s_1.smt", name));
        Process prepareFormula = Runtime.getRuntime().exec(new String[] {
            "java",
            "-classpath",
            System.getProperty("java.class.path"),
            "dev.fmsea.driver.Main",
            "smt2-format",
            dom1ActualFullSmtOutput.toString(),
            dom2ActualFullSmtOutput.toString(),
            actualFormulaOutput.toString(),
        });
        prepareFormula.waitFor(60l, TimeUnit.SECONDS);
        Process z3 = Runtime.getRuntime().exec(new String[] {
            "z3",
            "-smt2",
            actualFormulaOutput.toString(),
        });
        z3.waitFor(60l, TimeUnit.SECONDS);
        BufferedReader reader = new BufferedReader(new InputStreamReader(z3.getInputStream(), StandardCharsets.UTF_8));
        String actualResults = reader.lines().collect(Collectors.joining("\n")).trim();
        assertAll(() -> assertEquals(Optional.of(dom1ExpectedFull),
            readFile(dom1ActualFullSmtOutput),
            "Zones Full Output is Different"),
            () -> assertEquals(Optional.of(dom2ExpectedFull),
                readFile(dom2ActualFullSmtOutput),
                "Predicates Full Output is Different"),
            () -> assertEquals(Optional.of(expectedFormula),
                readFile(actualFormulaOutput),
                "Entailed Formulas Output is Different"),
            () -> assertEquals(expectedResults,
                actualResults,
                "Sat/Unsat Results are Different"));
        reader.close();
    }
}
