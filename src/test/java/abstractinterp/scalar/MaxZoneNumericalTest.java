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

import abstractinterp.scalar.state.MaxZoneState;
import abstractinterp.scalar.state.factory.MaxZoneStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class MaxZoneNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFormulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.constantValuePropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.constantMathPropagation.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.branching.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.looping.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.example5.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.nonsenseExample.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.neqLoop.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.getArrowSubset.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.intervalComparison.out").split("\n");

        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.transverseZero.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                             .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.fibonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.tribonacci.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.factorial.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }


    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.decode.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }

    @Test
    void testSwap() {
        Body body = JimpleProvider.swap();
        IntegerAnalysis<MaxZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new MaxZoneStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReportFull().split("\n");
        String[] expected = readResourcesFile("zones.swap.out").split("\n");
        assertEquals(expected.length, actual.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], actual[i])));
    }
}
