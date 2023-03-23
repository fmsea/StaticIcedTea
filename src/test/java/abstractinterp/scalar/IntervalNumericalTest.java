package abstractinterp.scalar;

import java.util.Map;
import java.util.HashMap;

import soot.Scene;
import soot.Body;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class IntervalNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @Test
    void testConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateReport().trim();
        String expected = readResourcesFile("int.constantValuePropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFomulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.constantValuePropagation.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("constantMath");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateReport().trim();
        String expected = readResourcesFile("int.constantMathPropagation.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWithConstantArithmaticPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.constantMathPropagation.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testIfStatementPropagation() {
        Body body = JimpleProvider.simpleIfStatement("simpleIf");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateReport().trim();
        String expected = readResourcesFile("int.branching.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaReportWhenIfStatement() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.branching.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testWhileStatementPropagation() {
        Body body = JimpleProvider.simpleLoopStatement("simpleLoop");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateReport().trim();
        String expected = readResourcesFile("int.looping.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTFormulaWhenWhileStatement() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.looping.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateReport().trim();
        String expected = readResourcesFile("int.example5.out");
        assertEquals(expected, actual);
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.example5.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.nonsenseExample.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.neqLoop.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.getArrowSubset.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testIntervalComparison() {
        Body body = JimpleProvider.intervalComparison();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.intervalComparison.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTransverseZero() {
        Body body = JimpleProvider.transverseZero();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.transverseZero.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFibonacci() {
        Body body = JimpleProvider.fibonacci();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.fibonacci.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testTribonacci() {
        Body body = JimpleProvider.tribonacci();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.tribonacci.smt.out");
        assertEquals(expected, actual);
    }

    @Test
    void testFactorial() {
        Body body = JimpleProvider.factorial();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.factorial.out");
        assertEquals(expected, actual);
    }

    @Test
    void testDecode() {
        Body body = JimpleProvider.decode();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String actual = analysis.generateSMTReport().trim();
        String expected = readResourcesFile("int.decode.out");
        assertEquals(expected, actual);
    }
}
