package abstractinterp.scalar;

import soot.Scene;
import soot.Body;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.DifferenceBoundedState;
import abstractinterp.scalar.state.factory.DifferenceBoundedStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class DBSNumericalTest {

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
        String[] expected = new String[] {
            "2 l1 = 6:<constant_testSootClass: int constant_test(int)>",
            "l1->(= l1 6)",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testSMTFormulaReportWithConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[]{
            "1 l0 = 3:<moreConstantMathSootClass: void moreConstantMath()>",
            "l0->(= l0 3)",
            "2 l1 = l0 + 6:<moreConstantMathSootClass: void moreConstantMath()>",
            "l1->(and (= l1 (+ 6 l0)) (= l1 9))",
            "3 l2 = l1 - l0:<moreConstantMathSootClass: void moreConstantMath()>",
            "l2->(= l2 6)",
            "4 l3 = l2 * -1:<moreConstantMathSootClass: void moreConstantMath()>",
            "l3->(= l3 (- 6))",
            "5 l0 = l3 / l2:<moreConstantMathSootClass: void moreConstantMath()>",
            "l0->(and (= l1 (+ 6 l0)) (= l0 (- 1)))"
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testSMTWhenBranching() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 l0 = 4:<anotherSimpleIfSootClass: void anotherSimpleIf()>",
            "l0->(= l0 4)",
            "2 l1 = 0:<anotherSimpleIfSootClass: void anotherSimpleIf()>",
            "l1->(= l1 0)",
            "3 l2 = 0:<anotherSimpleIfSootClass: void anotherSimpleIf()>",
            "l2->(= l2 0)",
            "4 if l0 >= 3 goto l3 = 6:<anotherSimpleIfSootClass: void anotherSimpleIf()>",
            "6 l3 = 6:<anotherSimpleIfSootClass: void anotherSimpleIf()>",
            "l3->(= l3 6)",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testSMTWhenLooping() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 l0 = 5:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l0->(= l0 5)",
            "2 l1 = 0:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(= l1 0)",
            "3 if l1 >= 5 goto l3 = l0 + l1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(<= l1 4)",
            "l1f->(and (<= l1 5) (>= l1 5))",
            "4 l1 = l1 + 1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(<= l1 5)",
            "6 l3 = l0 + l1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l3->(or (<= l3 0) (> l3 0))",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<DifferenceBoundedState> analysis =
            new IntegerAnalysis<>(body, 2, new DifferenceBoundedStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "2 b2 = 1:<test.Example1M: int example_5(int)>",
            "b2->(= b2 1)",
            "3 b3 = 3:<test.Example1M: int example_5(int)>",
            "b3->(= b3 3)",
            "4 if b3 != 0 goto i4 = b3 + b2:<test.Example1M: int example_5(int)>",
            "7 i4 = b3 + b2:<test.Example1M: int example_5(int)>",
            "i4->(= i4 4)",
            "8 $i0 = b3 * i4:<test.Example1M: int example_5(int)>",
            "$i0->(= $i0 12)",
            "9 i5 = $i0 - 18:<test.Example1M: int example_5(int)>",
            "i5->(and (= i5 (+ (- 18) $i0)) (= i5 (- 6)))",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }
}
