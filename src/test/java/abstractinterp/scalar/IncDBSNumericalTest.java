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

import abstractinterp.scalar.state.IncDifferenceBoundedState;
import abstractinterp.scalar.state.factory.IncDifferenceBoundedStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class IncDBSNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFormulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.constantValuePropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.constantMathPropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.branching.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.looping.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.example5.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.nonsenseExample.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.neqLoop.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.getArrowSubset.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.intervalComparison.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.transverseZero.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                             .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.fibonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.tribonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.factorial.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<IncDifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new IncDifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("incdbs.decode.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }
}
