package dev.fmsea.absint.scalar.state.update.rules;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrixBuilder;
import dev.fmsea.absint.scalar.state.update.DefaultOctagonThunkVisitor;
import dev.fmsea.common.Locals;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.TADRReader;
import soot.Local;

public class OctagonReassignmentSumExprRuleTest extends OctagonRuleTest {

    private static final int N = 8;
    private static Local[] xs;
    private final OctagonInplaceAddExprRule rule = new OctagonInplaceAddExprRule();
    private final DefaultOctagonThunkVisitor visitor = new DefaultOctagonThunkVisitor();

    @BeforeAll
    static void setupLocals() {
        xs = new Local[] {
            Locals.get("x1"),
            Locals.get("x2"),
            Locals.get("x3"),
            Locals.get("x4"),
        };
    }

    @Test
    public void testCanUpdateExpressions() {
        assertAll(
            () -> assertTrue(rule.canUpdate(TADR.newReassignment(TADR.newVariable(xs[0]),
                TADR.newValue(2)))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ x1 1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ 1 x1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (- x1 1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (- 1 x1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(<= x1 (+ x2 1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(>= x1 (+ x1 1))"))),
            () -> assertFalse(rule.canUpdate(TADRReader.parse("(>= x1 (+ x2 1))")))
        );
    }

    @Test
    public void testCanEncodeSimpleExpression() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(4))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(0, 2, Constraint.of(2))
            .setConstraint(3, 1, Constraint.of(2))
            .close()
            .build();
        var thunks = rule.update(
            TADR.newReassignment(TADR.newVariable(xs[0]), TADR.newValue(2)),
            OctagonRuleTest::lookup);
        assertAll(
            () -> assertTrue(thunks.map(t -> t.accept(visitor, m, m)).reduce((a, b) -> a && b).orElse(false)),
            () -> assertEquals(Constraint.of(8), m.getConstraint(0, 1)),
            () -> assertEquals(Constraint.of(-6), m.getConstraint(1, 0)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(0, 2)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(3, 1))
        );
    }
}
