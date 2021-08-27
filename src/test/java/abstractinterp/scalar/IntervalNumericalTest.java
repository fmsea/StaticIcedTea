package abstractinterp.scalar;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Scene;
import soot.Body;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;
import abstractinterp.scalar.state.providers.JimpleProvider;

public class IntervalNumericalTest {

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
        String[] actual = analysis.generateReport().split("\n");
        assertReportOutputEquals(new String[] {
                "l1 = 6 class soot.jimple.internal.JAssignStmt f->{l1=6}",
                "return l1 class soot.jimple.internal.JReturnStmt f->{l1=⟙}"},
            actual);
    }

    @Test
    void testSMTFomulaReportWithConstantValuePropagation() {
        Body body = JimpleProvider.constantJimpleMethod("constant_test", true);
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "2 l1 = 6:<constant_testSootClass: int constant_test(int)>",
            "l1->(= l1 6)"};
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testConstantMathPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("constantMath");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateReport().split("\n");
        assertReportOutputEquals(new String[] {
                "l0 = 3 class soot.jimple.internal.JAssignStmt f->{l0=3, l1=⟙, l2=⟙, l3=⟙}",
                "l1 = l0 + 6 class soot.jimple.internal.JAssignStmt f->{l0=3, l1=9, l2=⟙, l3=⟙}",
                "l2 = l1 - l0 class soot.jimple.internal.JAssignStmt f->{l0=3, l1=9, l2=6, l3=⟙}",
                "l3 = l2 * -1 class soot.jimple.internal.JAssignStmt f->{l0=3, l1=9, l2=6, l3=-6}",
                "l0 = l3 / l2 class soot.jimple.internal.JAssignStmt f->{l0=-1, l1=9, l2=6, l3=-6}",
                "return class soot.jimple.internal.JReturnVoidStmt f->{l0=⟙, l1=⟙, l2=⟙, l3=⟙}"},
            actual);
    }

    @Test
    void testSMTFormulaReportWithConstantArithmaticPropagation() {
        Body body = JimpleProvider.binaryArithmaticMethod("moreConstantMath");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
                "1 l0 = 3:<moreConstantMathSootClass: void moreConstantMath()>",
                "l0->(= l0 3)",
                "2 l1 = l0 + 6:<moreConstantMathSootClass: void moreConstantMath()>",
                "l1->(= l1 9)",
                "3 l2 = l1 - l0:<moreConstantMathSootClass: void moreConstantMath()>",
                "l2->(= l2 6)",
                "4 l3 = l2 * -1:<moreConstantMathSootClass: void moreConstantMath()>",
                "l3->(= l3 (- 6))",
                "5 l0 = l3 / l2:<moreConstantMathSootClass: void moreConstantMath()>",
                "l0->(= l0 (- 1))"
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testIfStatementPropagation() {
        Body body = JimpleProvider.simpleIfStatement("simpleIf");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateReport().split("\n");
        assertReportOutputEquals(new String[] {
                "l0 = 4 class soot.jimple.internal.JAssignStmt f->{l0=4, l1=⟙, l2=⟙, l3=⟙}",
                "l1 = 0 class soot.jimple.internal.JAssignStmt f->{l0=4, l1=0, l2=⟙, l3=⟙}",
                "l2 = 0 class soot.jimple.internal.JAssignStmt f->{l0=4, l1=0, l2=0, l3=⟙}",
                "if l0 >= 3 goto l3 = 6 class soot.jimple.internal.JIfStmt f->{l0=⟘, l1=0, l2=0, l3=⟙}",
                "if l0 >= 3 goto l3 = 6 b->[{l0=4, l1=0, l2=0, l3=⟙}]",
                "l3 = l1 / l2 class soot.jimple.internal.JAssignStmt f->{l0=⟘, l1=0, l2=0, l3=⟙}",
                "l3 = 6 class soot.jimple.internal.JAssignStmt f->{l0=4, l1=0, l2=0, l3=6}",
                "return class soot.jimple.internal.JReturnVoidStmt f->{l3=⟙, l1=⟙, l2=⟙, l0=⟙}"},
            actual);
    }

    @Test
    void testSMTFormulaReportWhenIfStatement() {
        Body body = JimpleProvider.simpleIfStatement("anotherSimpleIf");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
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
            "l3->(= l3 6)"
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testWhileStatementPropagation() {
        Body body = JimpleProvider.simpleLoopStatement("simpleLoop");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateReport().split("\n");
        assertReportOutputEquals(new String[] {
                "l0 = 5 class soot.jimple.internal.JAssignStmt f->{l0=5, l2=⟙, l1=⟙, l3=⟙}",
                "l1 = 0 class soot.jimple.internal.JAssignStmt f->{l0=5, l2=⟙, l1=0, l3=⟙}",
                "if l1 >= 5 goto l3 = l0 + l1 class soot.jimple.internal.JIfStmt f->{l0=⟙, l2=⟙, l1=(-∞, 4], l3=⟙}",
                "if l1 >= 5 goto l3 = l0 + l1 b->[{l0=⟙, l2=⟙, l1=[5, ∞), l3=⟙}]",
                "l1 = l1 + 1 class soot.jimple.internal.JAssignStmt f->{l0=⟙, l2=⟙, l1=⟙, l3=⟙}",
                "goto [?= (branch)] class soot.jimple.internal.JGotoStmt f->{l0=⟙, l2=⟙, l1=⟙, l3=⟙}",
                "goto [?= (branch)] b->[{l0=⟙, l2=⟙, l1=⟙, l3=⟙}]",
                "l3 = l0 + l1 class soot.jimple.internal.JAssignStmt f->{l0=⟙, l2=⟙, l1=[5, ∞), l3=⟙}",
                "return class soot.jimple.internal.JReturnVoidStmt f->{l0=⟙, l2=⟙, l1=⟙, l3=⟙}"},
            actual);
    }

    @Test
    void testSMTFormulaWhenWhileStatement() {
        Body body = JimpleProvider.simpleLoopStatement("anotherSimpleLoop");
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 l0 = 5:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l0->(= l0 5)",
            "2 l1 = 0:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(= l1 0)",
            "3 if l1 >= 5 goto l3 = l0 + l1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(<= l1 4)",
            "l1f->(>= l1 5)",
            "4 l1 = l1 + 1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l1->(or (>= l1 0) (< l1 0))",
            "6 l3 = l0 + l1:<anotherSimpleLoopSootClass: void anotherSimpleLoop()>",
            "l3->(or (>= l3 0) (< l3 0))"
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IntervalBoxState>  analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateReport().split("\n");
        String[] expected = new String[] {
            "i1 := @parameter0: int class soot.jimple.internal.JIdentityStmt f->" +
            "{b3=⟙, i1=⟙, b2=⟙, $i0=⟙, i4=⟙, i5=⟙}",
            "b2 = 1 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=⟙, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}",
            "b3 = 3 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=3, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}",
            "if b3 != 0 goto i4 = b3 + b2 class soot.jimple.internal.JIfStmt f->" +
            "{b3=⟘, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}",
            "if b3 != 0 goto i4 = b3 + b2 b->" +
            "[{b3=3, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}]",
            "i4 = b3 - b2 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=⟘, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}",
            "goto [?= $i0 = b3 * i4] class soot.jimple.internal.JGotoStmt f->" +
            "{b3=⟙, i1=⟙, b2=⟙, $i0=⟙, i4=⟙, i5=⟙}",
            "goto [?= $i0 = b3 * i4] b->" +
            "[{b3=⟘, i1=⟙, b2=1, $i0=⟙, i4=⟙, i5=⟙}]",
            "i4 = b3 + b2 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=3, i1=⟙, b2=1, $i0=⟙, i4=4, i5=⟙}",
            "$i0 = b3 * i4 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=3, i1=⟙, b2=1, $i0=12, i4=4, i5=⟙}",
            "i5 = $i0 - 18 class soot.jimple.internal.JAssignStmt f->" +
            "{b3=3, i1=⟙, b2=1, $i0=12, i4=4, i5=-6}",
            "return i5 class soot.jimple.internal.JReturnStmt f->" +
            "{b3=⟙, i1=⟙, b2=⟙, $i0=⟙, i4=⟙, i5=⟙}"
        };
        assertReportOutputEquals(expected, actual);
    }

    @Test
    void testSMTExample5() {
        Body body = JimpleProvider.example5();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
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
            "i5->(= i5 (- 6))",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testNonsenseExample() {
        Body body = JimpleProvider.nonsense();
        IntegerAnalysis<IntervalBoxState> analysis = new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 l0 = 3:<test.Nonsense: void decode()>",
            "l0->(= l0 3)",
            "2 l1 = 4:<test.Nonsense: void decode()>",
            "l1->(= l1 4)",
            "3 l2 = 1:<test.Nonsense: void decode()>",
            "l2->(= l2 1)",
            "4 if l3 > 20 goto return:<test.Nonsense: void decode()>",
            "l3->(<= l3 20)",
            "l3f->(>= l3 21)",
            "5 l4 = l3 % 2:<test.Nonsense: void decode()>",
            "l4->(or (>= l4 0) (< l4 0))",
            "6 if l1 == 0 goto l3 = l3 + 1:<test.Nonsense: void decode()>",
            "l1->(or (>= l1 0) (< l1 0))",
            "l1f->(= l1 0)",
            "7 l3 = l0 - 2:<test.Nonsense: void decode()>",
            "l3->(or (>= l3 0) (< l3 0))",
            "9 l3 = l3 + 1:<test.Nonsense: void decode()>",
            "l3->(or (>= l3 0) (< l3 0))",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testNeqLoop() {
        Body body = JimpleProvider.neqLoop();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 l1 = 4:<test.neqBranch: void neq()>",
            "l1->(= l1 4)",
            "2 if l0 != 0 goto return:<test.neqBranch: void neq()>",
            "l0->(= l0 0)",
            "l0f->(or (>= l0 0) (< l0 0))",
            "3 l2 = l1 + 1:<test.neqBranch: void neq()>",
            "l2->(or (>= l2 0) (< l2 0))",
        };
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void testGetArrowSubset() {
        Body body = JimpleProvider.ballonGetArrow();
        IntegerAnalysis<IntervalBoxState> analysis =
            new IntegerAnalysis<>(body, 2, new IntervalBoxStateFactory());
        analysis.runAnalysis();
        String[] actual = analysis.generateSMTReport().split("\n");
        String[] expected = new String[] {
            "1 b0 = 0:<test.ballonFactory: void getArrow()>",
            "b0->(= b0 0)",
            "2 b1 = 50:<test.ballonFactory: void getArrow()>",
            "b1->(= b1 50)",
            "3 b2 = 60:<test.ballonFactory: void getArrow()>",
            "b2->(= b2 60)",
            "4 $b25 = neg b2:<test.ballonFactory: void getArrow()>",
            "$b25->(= $b25 (- 60))",
            "5 $i26 = $b25 / 2:<test.ballonFactory: void getArrow()>",
            "$i26->(= $i26 (- 30))",
        };

        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    private void assertReportOutputEquals(String[] expected, String[] actual) {
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            Assertions.assertEquals(
                    expected[i].substring(0, expected[i].lastIndexOf("->")),
                    actual[i].substring(0, actual[i].lastIndexOf("->")));
            Assertions.assertTrue(areLocalsEqual(expected[i], actual[i]));
        }
    }

    private boolean areLocalsEqual(String expected, String result) {
        Map<String, String> expectedLocals = parseLocals(expected);
        Map<String, String> resultLocals = parseLocals(result);
        if (expectedLocals.keySet().size() != resultLocals.keySet().size()) {
            return false;
        }
        for (Map.Entry<String, String> l : expectedLocals.entrySet()) {
            if (!resultLocals.containsKey(l.getKey())) {
                return false;
            } else if (!resultLocals.get(l.getKey()).equals(l.getValue())) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> parseLocals(String statement) {
        Pattern pattern = Pattern.compile("([a-z0-9]{2})=(\\[-*\\d+, -*\\d+\\]|\\d+|⟙|⟘)");
        Map<String, String> locals = new HashMap<>();
        String values = statement.substring(statement.indexOf('{') + 1, statement.indexOf('}'));
        Matcher r = pattern.matcher(values);
        while (r.find()) {
            locals.put(r.group(1), r.group(2));
        }
        return locals;
    }
}
