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

import abstractinterp.scalar.state.DifferenceBoundedState;
import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.DifferenceBoundedStateFactory;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;
import processing.Smt2Format;
import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class DBSIntervalComparativeTest {

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
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l1 = 6:<z3_constant_testSootClass: int z3_constant_test()>",
                "l1",
                "sat",
                "sat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testConstantArithmatic() {
        Body body = JimpleProvider.binaryArithmaticMethod("z3ConstantMath");
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l0 = 3:<z3ConstantMathSootClass: void z3ConstantMath()>",
                "l0",
                "sat",
                "sat",
                "2 l1 = l0 + 6:<z3ConstantMathSootClass: void z3ConstantMath()>",
                "l1",
                "sat",
                "unsat",
                "3 l2 = l1 - l0:<z3ConstantMathSootClass: void z3ConstantMath()>",
                "l2",
                "sat",
                "sat",
                "4 l3 = l2 * -1:<z3ConstantMathSootClass: void z3ConstantMath()>",
                "l3",
                "sat",
                "sat",
                "5 l0 = l3 / l2:<z3ConstantMathSootClass: void z3ConstantMath()>",
                "l0",
                "sat",
                "unsat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testBranchingStatement() {
        Body body = JimpleProvider.simpleIfStatement("z3_simpleIf");
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l0 = 4:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "l0",
                "sat",
                "sat",
                "2 l1 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "l1",
                "sat",
                "sat",
                "3 l2 = 0:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "l2",
                "sat",
                "sat",
                "4 if l0 >= 3 goto l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "l0f",
                "sat",
                "sat",
                "5 l3 = l1 / l2:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "6 l3 = 6:<z3_simpleIfSootClass: void z3_simpleIf()>",
                "l3",
                "sat",
                "sat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testLoopingStatement() {
        Body body = JimpleProvider.simpleLoopStatement("z3_simple_loop");
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l0 = 5:<z3_simple_loopSootClass: void z3_simple_loop()>",
                "l0",
                "sat",
                "sat",
                "2 l1 = 0:<z3_simple_loopSootClass: void z3_simple_loop()>",
                "l1",
                "sat",
                "sat",
                "3 if l1 >= 5 goto l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>",
                "l1",
                "sat",
                "sat",
                "l1f",
                "sat",
                "unsat",
                "4 l1 = l1 + 1:<z3_simple_loopSootClass: void z3_simple_loop()>",
                "l1",
                "sat",
                "sat",
                "6 l3 = l0 + l1:<z3_simple_loopSootClass: void z3_simple_loop()>",
                "l3",
                "sat",
                "sat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
            "2 b2 = 1:<test.Example1M: int example_5(int)>",
            "b2",
            "sat",
            "sat",
            "3 b3 = 3:<test.Example1M: int example_5(int)>",
            "b3",
            "sat",
            "sat",
            "4 if b3 != 0 goto i4 = b3 + b2:<test.Example1M: int example_5(int)>",
            "b3f",
            "sat",
            "sat",
            "5 i4 = b3 - b2:<test.Example1M: int example_5(int)>",
            "7 i4 = b3 + b2:<test.Example1M: int example_5(int)>",
            "i4",
            "sat",
            "sat",
            "8 $i0 = b3 * i4:<test.Example1M: int example_5(int)>",
            "$i0",
            "sat",
            "sat",
            "9 i5 = $i0 - 18:<test.Example1M: int example_5(int)>",
            "i5",
            "sat",
            "unsat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l0 = 3:<test.Nonsense: void decode()>",
                "l0",
                "sat",
                "sat",
                "2 l1 = 4:<test.Nonsense: void decode()>",
                "l1",
                "sat",
                "sat",
                "3 l2 = 1:<test.Nonsense: void decode()>",
                "l2",
                "sat",
                "sat",
                "4 if l3 > 20 goto return:<test.Nonsense: void decode()>",
                "l3",
                "sat",
                "sat",
                "l3f",
                "sat",
                "sat",
                "5 l4 = l3 % 2:<test.Nonsense: void decode()>",
                "l4",
                "sat",
                "sat",
                "6 if l1 == 0 goto l3 = l3 + 1:<test.Nonsense: void decode()>",
                "l1",
                "sat",
                "sat",
                "l1f",
                "sat",
                "sat",
                "7 l3 = l0 - 2:<test.Nonsense: void decode()>",
                "l3",
                "sat",
                "unsat",
                "9 l3 = l3 + 1:<test.Nonsense: void decode()>",
                "l3",
                "sat",
                "sat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 l1 = 4:<test.neqBranch: void neq()>",
                "l1",
                "sat",
                "sat",
                "2 if l0 != 0 goto return:<test.neqBranch: void neq()>",
                "l0",
                "sat",
                "sat",
                "l0f",
                "sat",
                "sat",
                "3 l2 = l1 + 1:<test.neqBranch: void neq()>",
                "l2",
                "sat",
                "unsat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 b0 = 0:<test.ballonFactory: void getArrow()>",
                "b0",
                "sat",
                "sat",
                "2 b1 = 50:<test.ballonFactory: void getArrow()>",
                "b1",
                "sat",
                "sat",
                "3 b2 = 60:<test.ballonFactory: void getArrow()>",
                "b2",
                "sat",
                "sat",
                "4 $b25 = neg b2:<test.ballonFactory: void getArrow()>",
                "$b25",
                "sat",
                "sat",
                "5 $i26 = $b25 / 2:<test.ballonFactory: void getArrow()>",
                "$i26",
                "sat",
                "sat",
            }).collect(Collectors.joining("\n"));
        Assertions.assertTrue(runComparison(dbsAnalysis.generateSMTReport(),
                                            intervalAnalysis.generateSMTReport(),
                                            expected));
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<DifferenceBoundedState> dbsAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new DifferenceBoundedStateFactory());
        IntegerAnalysis<IntervalBoxState> intervalAnalysis =
            new IntegerAnalysis<>(this.solver, body, 2, new IntervalBoxStateFactory());
        dbsAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = Stream.of(new String[] {
                "1 u0 = x0:<test.ints: int compareIntervals()>",
                "u0",
                "sat",
                "unknown",
                "2 if x0 < 20 goto (branch):<test.ints: int compareIntervals()>",
                "x0",
                "sat",
                "unsat",
                "x0f",
                "sat",
                "unsat",
                "4 if x0 >= 0 goto w0 = x0 + u0:<test.ints: int compareIntervals()>",
                "x0",
                "sat",
                "unsat",
                "x0f",
                "sat",
                "unsat",
                "6 w0 = x0 + u0:<test.ints: int compareIntervals()>",
                "w0",
                "sat",
                "unsat",
            }).collect(Collectors.joining("\n"));
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
