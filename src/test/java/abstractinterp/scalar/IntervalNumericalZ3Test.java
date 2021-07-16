package abstractinterp.scalar;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import soot.Body;
import soot.Scene;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;
import processing.Smt2Format;
import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class IntervalNumericalZ3Test {

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
        Assertions.assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.constantJimpleMethod("z3_constant_test");
        String oracle = "1 l1 = 6:<z3_constant_testSootClass: int z3_constant_test()>\n" +
            "l1->(= l1 6)\n";
        String expected = "1 l1 = 6:<z3_constant_testSootClass: int z3_constant_test()>\n" +
            "l1\nsat\nsat";
        Assertions.assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testConstantArithmaticPropagation() {
        Assertions.assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.binaryArithmaticMethod("z3ConstantMath");
        String oracle = "1 l0 = 3:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l0->(= l0 3)\n" +
            "2 l1 = l0 + 6:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l1->(= l1 9)\n" +
            "3 l2 = l1 - l0:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l2->(= l2 6)\n" +
            "4 l3 = l2 * -1:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l3->(= l3 (- 6))\n" +
            "5 l0 = l3 / l2:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l0->(= l0 (- 1))\n";
        String expected = "1 l0 = 3:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l0\nsat\nsat\n" +
            "2 l1 = l0 + 6:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l1\nsat\nsat\n" +
            "3 l2 = l1 - l0:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l2\nsat\nsat\n" +
            "4 l3 = l2 * -1:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l3\nsat\nsat\n" +
            "5 l0 = l3 / l2:<z3ConstantMathSootClass: void z3ConstantMath()>\n" +
            "l0\nsat\nsat";
        Assertions.assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testIfStatementPropagation() {
        Assertions.assertTrue(Files.exists(this.z3TestFile));
        Body body = JimpleProvider.simpleIfStatement("z3_simpleIf");
        String oracle = "1 l0 = 4:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l0->(= l0 4)\n" +
            "2 l1 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l1->(= l1 0)\n" +
            "3 l2 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l2->(= l2 0)\n" +
            "4 if l0 >= 3 goto l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "6 l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l3->(= l3 6)\n";
        String expected = "1 l0 = 4:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l0\nsat\nsat\n" +
            "2 l1 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l1\nsat\nsat\n" +
            "3 l2 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l2\nsat\nsat\n" +
            "4 if l0 >= 3 goto l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "\n(error \"line 40 column 22: invalid sorted variables invalid sort, unexpected ')'\")\n" +
            "sat\n" +
            "(error \"line 45 column 22: invalid sorted variables invalid sort, unexpected ')'\")\n" +
            "sat\n" +
            "6 l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>\n" +
            "l3\nsat\nsat";
        Assertions.assertTrue(runAnalysis(body, oracle, expected));
    }

    @Test
    void testWhileStatementPropagation() {
        Body body = JimpleProvider.simpleLoopStatement("z3_simple_loop");
        String oracle = "1 l0 = 5:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l0->(= l0 5)\n" +
            "2 l1 = 0:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1->(= l1 0)\n" +
            "3 if l1 >= 5 goto l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1->(or (>= l1 0) (< l1 0))\n" +
            "l1f->(or (>= l1 0) (< l1 0))\n" +
            "4 l1 = l1 + 1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1->(or (>= l1 0) (< l1 0))\n" +
            "6 l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l3->(or (>= l3 0) (< l3 0))\n";
        String expected = "1 l0 = 5:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l0\nsat\nsat\n" +
            "2 l1 = 0:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1\nsat\nsat\n" +
            "3 if l1 >= 5 goto l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1\nsat\nsat\nl1f\nsat\nsat\n" +
            "4 l1 = l1 + 1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l1\nsat\nsat\n" +
            "6 l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>\n" +
            "l3\nsat\nsat";
        Assertions.assertTrue(runAnalysis(body, oracle, expected));
    }

    private boolean runAnalysis(Body body, String oracle, String expectedZ3Output) {
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        Reader actual = new StringReader(analysis.generateSMTReport());
        Reader expected = new StringReader(oracle);
        try {
            Writer writer = new FileWriter(this.z3TestFile.toFile());
            Smt2Format.SMT2Format(expected, actual, writer);
            Process z3 = Runtime.getRuntime().exec(new String[] {"z3",
                                                                 "-smt2",
                                                                 this.z3TestFile.toString()});
            z3.waitFor(60l, TimeUnit.SECONDS);
            String output = new BufferedReader(new InputStreamReader(z3.getInputStream(),
                                                                     StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            Assertions.assertEquals(expectedZ3Output, output);
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
