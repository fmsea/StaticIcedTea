package abstractinterp.scalar;

import soot.Scene;
import soot.Body;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.ZoneState;
import abstractinterp.scalar.state.factory.ZoneStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class ZoneNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFormulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.constantValuePropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.constantMathPropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.branching.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.looping.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.example5.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.nonsenseExample.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.neqLoop.out");
        assertEquals(expected, actual);
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.getArrowSubset.out");
        assertEquals(expected, actual);
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.intervalComparison.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.transverseZero.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.fibonacci.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.tribonacci.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.factorial.out");
        assertEquals(expected, actual);
    }


    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.decode.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSwap() {
        Body body = JimpleProvider.swap();
        IntegerAnalysis<ZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new ZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReportFull().trim();
        String expected = readResourcesFile("zones.swap.out");
        assertEquals(expected, actual);
    }
}
