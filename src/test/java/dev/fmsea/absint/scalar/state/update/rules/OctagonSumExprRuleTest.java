package dev.fmsea.absint.scalar.state.update.rules;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.update.DefaultOctagonThunkVisitor;
import dev.fmsea.common.Locals;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.TADRReader;
import soot.Local;

public class OctagonSumExprRuleTest extends OctagonRuleTest {

    private static final int N = 8;
    private static Set<Local> locals;
    private static Local[] xs;
    private final OctagonSumExprRule rule = new OctagonSumExprRule();
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
    public void testCanUpdateExpressions() {
        assertAll(
            () -> assertTrue(rule.canUpdate(TADRReader.parse("(<= x1 (- 0 x2))"))),
            () -> assertTrue(rule.canUpdate(TADR.newLeExpr(TADR.newVariable(xs[0]),
                TADR.newSubExpr(TADR.newValue(0), TADR.newVariable(xs[1]))))),
            () -> assertTrue(rule.canUpdate(TADR.newLeExpr(TADR.newVariable(xs[0]),
                TADR.newSubExpr(TADR.newValue(Interval32Box.of(-2, 2)), TADR.newVariable(xs[1]))))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ 0 x2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(>= x1 (- 0 x2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (- x1 2))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (- 2 x1))"))));
    }

    @Test
    public void testCanEncodeSimpleExpression() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
        var thunks = rule.update(TADRReader.parse("(<= x1 (- 0 x2))"), OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 3))
        );
    }

    @Test
    public void testCanEncodeIntervalExpression() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
        var thunks = rule.update(TADR.newLeExpr(TADR.newVariable(xs[0]),
            TADR.newSubExpr(TADR.newValue(Interval32Box.of(-1, 2)), TADR.newVariable(xs[1]))),
            OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(0, 3)));
    }
}
