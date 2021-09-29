package abstractinterp.scalar;

import java.util.stream.IntStream;
import soot.Scene;
import soot.Body;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.DifferenceBoundedState;
import abstractinterp.scalar.state.factory.DifferenceBoundedStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class DBSNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFormulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.constantValuePropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.constantMathPropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.branching.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.looping.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.example5.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.nonsenseExample.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.neqLoop.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.getArrowSubset.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.intervalComparison.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.transverseZero.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                             .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.fibonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = readResourcesFile("dbs.tribonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }
}
