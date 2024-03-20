package abstractinterp.scalar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.providers.JimpleProvider;
import soot.Body;
import soot.Scene;

public class IntervalNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testSMTFomulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.constantValuePropagation.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWithConstantArithmaticPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.constantMathPropagation.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWhenIfStatement() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.branching.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaWhenWhileStatement() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.looping.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.example5.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.nonsenseExample.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.neqLoop.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.getArrowSubset.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.intervalComparison.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.transverseZero.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.fibonacci.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.tribonacci.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.factorial.out");
        assertEquals(expected, actual);
    }

    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, IntervalBoxState.class);
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile("int.decode.out");
        assertEquals(expected, actual);
    }
}
