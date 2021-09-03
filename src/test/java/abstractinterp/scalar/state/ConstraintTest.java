package abstractinterp.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ConstraintTest {

    @Test
    void testIsTop() {
        assertAll(() -> assertFalse(Constraint.BOT().isTop()),
                  () -> assertFalse(new Constraint(0, PredicateType.Lt).isTop()),
                  () -> assertTrue(Constraint.TOP().isTop()));
    }

    @Test
    void testConstraintTopNegate() {
        assertEquals(new Constraint(Integer.MIN_VALUE + 1,
                                    PredicateType.Ge),
                     Constraint.TOP().negate());
    }

    @Test
    void testConstraintAdditionOverlfow() {
        Constraint x = new Constraint(Integer.MAX_VALUE - 2,
                                      PredicateType.Eq);
        Constraint y = new Constraint(3, PredicateType.Eq);
        assertEquals(Integer.MAX_VALUE, x.add(y).bound());
    }

    @Test
    void testConstraintSubtractionUnderflow() {
        Constraint top = Constraint.TOP().negate();
        assertEquals(Integer.MIN_VALUE + 1,
                     top.subtract(new Constraint(1, PredicateType.Eq)).bound());
    }

    @Test
    void testConstraintMultiplyOverflow() {
        Constraint x = new Constraint(Integer.MAX_VALUE - 20,
                                      PredicateType.Eq);
        Constraint y = new Constraint(2, PredicateType.Eq);
        assertEquals(Constraint.TOP(), x.multiply(y));
    }

    @Test
    void testConstraintDivisionOverflow() {
        Constraint x = new Constraint(Integer.MIN_VALUE, PredicateType.Eq);
        Constraint y = new Constraint(-1, PredicateType.Eq);
        assertEquals(Constraint.TOP(), x.divide(y));
    }

    @Test
    void copyConstraint() {
    }

    @Test
    void testBottomConstraintsAreBottom() {
        assertAll(() -> assertEquals(Constraint.BOT(),
                                     Constraint.TOP().add(Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.BOT().add(Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.TOP().subtract(Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.BOT().subtract(Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.TOP().multiply(Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.BOT().multiply(Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.TOP().divide(Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.BOT().divide(Constraint.TOP())));
    }

    @Test
    void testConstraintMax() {
        assertAll(() -> assertEquals(new Constraint(0),
                                      Constraint.max(new Constraint(0), new Constraint(-1))),
                  () -> assertEquals(new Constraint(0),
                                     Constraint.max(new Constraint(-1), new Constraint(0))),
                  () -> assertEquals(new Constraint(0, PredicateType.Le),
                                     Constraint.max(new Constraint(+0, PredicateType.Le),
                                                    new Constraint(-1, PredicateType.Le))),
                  () -> assertEquals(new Constraint(2, PredicateType.Le),
                                     Constraint.max(new Constraint(-2, PredicateType.Le),
                                                    new Constraint(+2, PredicateType.Le))),
                  () -> assertEquals(new Constraint(0, PredicateType.Le),
                                     Constraint.max(new Constraint(0, PredicateType.Le),
                                                    new Constraint(0))),
                  () -> assertEquals(new Constraint(0, PredicateType.Le),
                                     Constraint.max(new Constraint(0),
                                              new Constraint(0, PredicateType.Le))));
    }
}
