package abstractinterp.scalar.state.update.rules;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.Interval32Box;
import abstractinterp.scalar.state.OctagonDifferenceBoundedMatrix;
import abstractinterp.scalar.state.update.DefaultOctagonThunkVisitor;
import common.Locals;
import soot.Local;
import tadr.TADR;
import tadr.TADRReader;

public class OctagonDiffExprRuleTest extends OctagonRuleTest {

    private static final int N = 8;
    private static Set<Local> locals;
    private static Local[] xs;
    private final OctagonDiffExprRule rule = new OctagonDiffExprRule();
    private final DefaultOctagonThunkVisitor visitor = new DefaultOctagonThunkVisitor();
    @BeforeAll
    static void setupLocals() {
        xs = new Local[] {
            Locals.get("x1"),
            Locals.get("x2"),
            Locals.get("x3"),
            Locals.get("x4"),
        };
        locals = Stream.of(xs).collect(Collectors.toSet());
    }

    @Test
    public void testCanUpdate() {
        assertAll(
            () -> assertTrue(rule.canUpdate(TADRReader.parse("(<= x1 (- x2 0))"))),
            () -> assertTrue(rule.canUpdate(TADR.newLeExpr(TADR.newVariable(xs[0]),
                TADR.newSubExpr(TADR.newVariable(xs[1]), TADR.newValue(0))))),
            () -> assertTrue(rule.canUpdate(TADR.newLeExpr(TADR.newVariable(xs[0]),
                TADR.newSubExpr(TADR.newVariable(xs[1]), TADR.newValue(Interval32Box.of(-2, 2)))))),
            () -> assertTrue(rule.canUpdate(TADRReader.parse("(<= x1 (+ 0 x2))"))),
            () -> assertTrue(rule.canUpdate(TADRReader.parse("(<= x1 (+ x2 0))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (- 0 x2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(>= x1 (- 0 x2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ x1 2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ 2 x1))"))));
    }

    @Test
    public void testCanEncodeSimpleExpression() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
        var thunks = rule.update(TADRReader.parse("(<= x1 (- x2 0))"), OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 2))
        );
    }

    @Test
    public void testCanEncodeIntervalExpression() {
        // x1 <= x2 - [-1, 2]
        // x1 <= x2 + [-2 , 1]
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
        var thunks = rule.update(TADR.newLeExpr(TADR.newVariable(xs[0]),
            TADR.newSubExpr(TADR.newVariable(xs[1]), TADR.newValue(Interval32Box.of(-1, 2)))),
            OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(1), m.getConstraint(0, 2)));
    }

    @Test
    public void testCanEncodeIntervalExpressionCorrectly() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
        var thunks = rule.update(TADR.newLeExpr(TADR.newVariable(xs[0]),
            TADR.newAddExpr(TADR.newVariable(xs[1]), TADR.newValue(Interval32Box.of(1, 2)))),
            OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(0, 2)));
    }
}
