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
import java.util.stream.Stream;

import soot.Scene;
import soot.Body;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import abstractinterp.scalar.state.PADO01DifferenceBoundedState;
import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.PADO01DifferenceBoundedStateFactory;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;
import processing.Smt2Format;
import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class PADO01DBSIntervalComparativeTest extends AbstractNumericalTest {

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
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testConstantValue() {
        Body body = JimpleProvider.constantJimpleMethod("z3_constant_test");
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.constantValuePropagation.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testConstantArithmatic() {
        Body body = JimpleProvider.binaryArithmaticMethod("z3ConstantMath");
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.constantMathPropagation.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testBranchingStatement() {
        Body body = JimpleProvider.simpleIfStatement("z3_simpleIf");
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.branching.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testLoopingStatement() {
        Body body = JimpleProvider.simpleLoopStatement("z3_simple_loop");
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.looping.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.example5.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.nonsenseExample.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.neqLoop.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.getArrowSubset.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.intervalComparison.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<PADO01DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new PADO01DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile("pado01.int.fibonacci.smt.out");
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    private boolean runComparison(String left, String right, String oracle) {
        Reader leftReader = new StringReader(left);
        Reader rightReader = new StringReader(right);
        try {
            Writer writer = new FileWriter(this.z3TestFile.toFile());
            Smt2Format.SMT2Format(leftReader, rightReader, writer);
            Process z3 = Runtime.getRuntime().exec(new String[] {"z3",
                                                                 "-smt2",
                                                                 this.z3TestFile.toString()});
            z3.waitFor(60l, TimeUnit.SECONDS);
            String output = new BufferedReader(new InputStreamReader(z3.getInputStream(),
                                                                     StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            Assertions.assertEquals(oracle, output);
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
