package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TieredOctagonDifferenceBoundedMatrixTest {

    static int N = 6;

    @Test
    public void tetstRelationalVariablePromotionForget() {
        TieredOctagonDifferenceBoundedMatrix dbm = new TieredOctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(2))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(4, 5, Constraint.of(6))
            .setConstraint(5, 4, Constraint.of(6))
            .setConstraint(2, 4, Constraint.of(3))
            .setConstraint(5, 3, Constraint.of(3))
            .close()
            .build();

        assertAll(
            () -> assertEquals(Constraint.TOP(), dbm.getConstraint(2, 0)),
            () -> assertEquals(Constraint.TOP(), dbm.getConstraint(1, 3))
        );
    }

    @Test
    public void testRelationalVariablePromotion() {
        TieredOctagonDifferenceBoundedMatrix dbm = new TieredOctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(2))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(4, 5, Constraint.of(6))
            .setConstraint(5, 4, Constraint.of(6))
            .setConstraint(2, 4, Constraint.of(3))
            .setConstraint(5, 3, Constraint.of(3))
            .build();

        TieredOctagonDifferenceBoundedMatrix m = new TieredOctagonDifferenceBoundedMatrix(dbm);
        var thunks = List.of(
            ConstraintUpdateThunk.of(4, 5, Constraint.of(8)),
            ConstraintUpdateThunk.of(5, 4, Constraint.of(-8))
        );

        m.forgetConstraints(4);
        m.forgetConstraints(5);

        assertAll(
            () -> assertTrue(m.incrementalClosure(thunks, dbm)),
            () -> assertEquals(Constraint.of(5), m.getConstraint(2, 0)),
            () -> assertEquals(Constraint.of(5), m.getConstraint(1, 3))
        );
    }
}
