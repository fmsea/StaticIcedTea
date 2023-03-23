package abstractinterp.scalar;

import soot.Scene;
import soot.Body;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.IncZoneState;
import abstractinterp.scalar.state.factory.IncZoneStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class IncZoneNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFormulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.constantValuePropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.constantMathPropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.branching.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.looping.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.example5.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.nonsenseExample.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.neqLoop.out");
        assertEquals(expected, actual);
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.getArrowSubset.out");
        assertEquals(expected, actual);
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.intervalComparison.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.transverseZero.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.fibonacci.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.tribonacci.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.factorial.out");
        assertEquals(expected, actual);
    }


    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.decode.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSwap() {
        Body body = JimpleProvider.swap();
        IntegerAnalysis<IncZoneState> analysis =
            new IntegerAnalysis<>(body, 2, new IncZoneStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("zones.swap.out");
        assertEquals(expected, actual);
    }
}
