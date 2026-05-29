package dev.fmsea.absint.scalar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.fmsea.absint.scalar.state.IntervalBoxState;
import dev.fmsea.absint.scalar.state.providers.JimpleProvider;
import dev.fmsea.processing.Smt2FormatReachable;
import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverWrapperZ3;
import soot.Body;
import soot.Scene;

public class IntervalNumericalZ3Test extends AbstractNumericalTest {

    private SolverWrapper solver;
    private Path z3TestFile;

    @BeforeEach
    void setup() {
        try {
            this.solver = new SolverWrapperZ3();
            this.z3TestFile = Files.createTempFile("dfa-smt",
                                                   String.valueOf(System.nanoTime()));
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @AfterEach
    void teardown() {
        try {
            Files.deleteIfExists(this.z3TestFile);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @BeforeAll
    static void sootSuiteInitlize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }


    @Test
    void testConstantValuePropagation() {
        assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.constantJimpleMethod("z3_constant_test");
        String oracle = readResourcesFile("int.z3.constantValuePropagation.out");
        String expected = readResourcesFile("int.z3.constantValuePropagation.smt.out");
        assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testConstantArithmaticPropagation() {
        assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.binaryArithmaticMethod("z3ConstantMath");
        String oracle = readResourcesFile("int.z3.constantMathPropagation.out");
        String expected = readResourcesFile("int.z3.constantMathPropagation.smt.out");
        assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testIfStatementPropagation() {
        assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.simpleIfStatement("z3_simpleIf");
        String oracle = readResourcesFile("int.z3.branching.out");
        String expected = readResourcesFile("int.z3.branching.smt.out");
        assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testWhileStatementPropagation() {
        Body body = JimpleProvider.simpleLoopStatement("z3_simple_loop");
        String oracle = readResourcesFile("int.z3.looping.out");
        String expected = readResourcesFile("int.z3.looping.smt.out");
        assertTrue(runAnalysis(body, oracle, expected));
    }

    private boolean runAnalysis(Body body, String oracle, String expectedZ3Output) {
        IntegerAnalysis analysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(IntervalBoxState.class)
            .build();
        analysis.runAnalysis();
        Reader actual = new StringReader(generateReport(analysis));
        Reader expected = new StringReader(oracle);
        try {
            Writer writer = new FileWriter(this.z3TestFile.toFile());
            Smt2FormatReachable.Smt2FormatReachable(expected, actual, writer);
            Process z3 = Runtime.getRuntime().exec(new String[] {"z3",
                                                                 "-smt2",
                                                                 this.z3TestFile.toString()});
            z3.waitFor(60l, TimeUnit.SECONDS);
            String output = new BufferedReader(new InputStreamReader(z3.getInputStream(),
                                                                     StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(expectedZ3Output, output);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return false;
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }
}
