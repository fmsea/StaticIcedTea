package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import soot.jimple.IntConstant;

public class ConstraintProperties {

    @Property
    void constantAdditionTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                                  @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        assertEquals(new Constraint(x + y, PredicateType.Eq),
                     Constraint.transferBinary(IntConstant.v(x),
                                               IntConstant.v(y),
                                               BinaryOperator.ADDITION));
    }

    @Property
    void constantSubtractionTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                                     @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        assertEquals(new Constraint(x - y, PredicateType.Eq),
                     Constraint.transferBinary(IntConstant.v(x),
                                               IntConstant.v(y),
                                               BinaryOperator.SUBTRACTION));
    }

    @Property
    void constraintMultiplicationTransfer(@ForAll @IntRange(min=-32768, max=32768) int x,
                                          @ForAll @IntRange(min=-32768, max=32768) int y) {
        assertEquals(new Constraint(x * y, PredicateType.Eq),
                     Constraint.transferBinary(IntConstant.v(x),
                                               IntConstant.v(y),
                                               BinaryOperator.MULTIPLICATION));
    }

    @Property
    void constraintDivisionTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                                    @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        Constraint actual = Constraint.transferBinary(IntConstant.v(x),
                                                      IntConstant.v(y),
                                                      BinaryOperator.DIVISION);
        if (y == 0) {
            assertEquals(Constraint.BOT(), actual);
        } else {
            assertEquals(new Constraint(x / y, PredicateType.Eq),
                         actual);
        }
    }

    @Property
    void constraintTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                            @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     Constraint.transferBinary(IntConstant.v(x),
                                                               IntConstant.v(y),
                                                               BinaryOperator.MODULUS)),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.transferBinary(IntConstant.v(x),
                                                               IntConstant.v(y),
                                                               BinaryOperator.INVALID)));
    }

    @Property
    void constraintAddition(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                            @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        assertAll(() -> assertEquals(new Constraint(x + y),
                                     Constraint.add(new Constraint(x),
                                                    new Constraint(y))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Le),
                                     Constraint.add(new Constraint(x, PredicateType.Le),
                                                    new Constraint(y, PredicateType.Lt))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Le),
                                     Constraint.add(new Constraint(x, PredicateType.Lt),
                                                    new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Le),
                                     Constraint.add(new Constraint(x),
                                                    new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Le),
                                     Constraint.add(new Constraint(x, PredicateType.Le),
                                                    new Constraint(y))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Ge),
                                     Constraint.add(new Constraint(x, PredicateType.Ge),
                                                    new Constraint(y, PredicateType.Gt))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Ge),
                                     Constraint.add(new Constraint(x, PredicateType.Gt),
                                                    new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Ge),
                                     Constraint.add(new Constraint(x),
                                                    new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x + y, PredicateType.Ge),
                                     Constraint.add(new Constraint(x, PredicateType.Ge),
                                                    new Constraint(y))));
    }

    @Property
    void constraintSubtraction(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                               @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        assertAll(() -> assertEquals(new Constraint(x - y),
                                     Constraint.subtract(new Constraint(x),
                                                         new Constraint(y))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Le),
                                     Constraint.subtract(new Constraint(x, PredicateType.Le),
                                                         new Constraint(y, PredicateType.Lt))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Le),
                                     Constraint.subtract(new Constraint(x, PredicateType.Lt),
                                                         new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Le),
                                     Constraint.subtract(new Constraint(x),
                                                         new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Le),
                                     Constraint.subtract(new Constraint(x, PredicateType.Le),
                                                         new Constraint(y))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Ge),
                                     Constraint.subtract(new Constraint(x, PredicateType.Ge),
                                                         new Constraint(y, PredicateType.Gt))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Ge),
                                     Constraint.subtract(new Constraint(x, PredicateType.Gt),
                                                         new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Ge),
                                     Constraint.subtract(new Constraint(x),
                                                         new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x - y, PredicateType.Ge),
                                     Constraint.subtract(new Constraint(x, PredicateType.Ge),
                                                         new Constraint(y))));
    }

    @Property
    void constraintMultiplication(@ForAll @IntRange(min=-32768, max=32768) int x,
                                  @ForAll @IntRange(min=-32768, max=32768) int y) {
        assertAll(() -> assertEquals(new Constraint(x * y),
                                     Constraint.multiply(new Constraint(x),
                                                         new Constraint(y))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Le),
                                     Constraint.multiply(new Constraint(x, PredicateType.Le),
                                                         new Constraint(y, PredicateType.Lt))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Le),
                                     Constraint.multiply(new Constraint(x, PredicateType.Lt),
                                                         new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Le),
                                     Constraint.multiply(new Constraint(x),
                                                         new Constraint(y, PredicateType.Le))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Le),
                                     Constraint.multiply(new Constraint(x, PredicateType.Le),
                                                         new Constraint(y))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Ge),
                                     Constraint.multiply(new Constraint(x, PredicateType.Ge),
                                                         new Constraint(y, PredicateType.Gt))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Ge),
                                     Constraint.multiply(new Constraint(x, PredicateType.Gt),
                                                         new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Ge),
                                     Constraint.multiply(new Constraint(x),
                                                         new Constraint(y, PredicateType.Ge))),
                  () -> assertEquals(new Constraint(x * y, PredicateType.Ge),
                                     Constraint.multiply(new Constraint(x, PredicateType.Ge),
                                                         new Constraint(y))));
    }

    @Property
    void constraintDivision(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                            @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        if (y == 0) {
            assertAll(() -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Le),
                                                           new Constraint(y, PredicateType.Lt))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Lt),
                                                           new Constraint(y, PredicateType.Le))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y, PredicateType.Le))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Le),
                                                           new Constraint(y))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Ge),
                                                           new Constraint(y, PredicateType.Gt))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Gt),
                                                           new Constraint(y, PredicateType.Ge))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y, PredicateType.Ge))),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(new Constraint(x, PredicateType.Ge),
                                                           new Constraint(y))));
        } else {
            assertAll(() -> assertEquals(new Constraint(x / y),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Le),
                                         Constraint.divide(new Constraint(x, PredicateType.Le),
                                                           new Constraint(y, PredicateType.Lt))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Le),
                                         Constraint.divide(new Constraint(x, PredicateType.Lt),
                                                           new Constraint(y, PredicateType.Le))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Le),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y, PredicateType.Le))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Le),
                                         Constraint.divide(new Constraint(x, PredicateType.Le),
                                                           new Constraint(y))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Ge),
                                         Constraint.divide(new Constraint(x, PredicateType.Ge),
                                                           new Constraint(y, PredicateType.Gt))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Ge),
                                         Constraint.divide(new Constraint(x, PredicateType.Gt),
                                                           new Constraint(y, PredicateType.Ge))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Ge),
                                         Constraint.divide(new Constraint(x),
                                                           new Constraint(y, PredicateType.Ge))),
                      () -> assertEquals(new Constraint(x / y, PredicateType.Ge),
                                         Constraint.divide(new Constraint(x, PredicateType.Ge),
                                                           new Constraint(y))));
        }
    }

    @Property
    void bottomStaysBottom(@ForAll Constraint c) {
        Constraint bot = Constraint.BOT();
        assertAll(() -> assertEquals(Constraint.BOT(),
                                     c.add(bot)),
                  () -> assertEquals(Constraint.BOT(),
                                     bot.add(c)),
                  () -> assertEquals(Constraint.BOT(),
                                     c.subtract(bot)),
                  () -> assertEquals(Constraint.BOT(),
                                     bot.subtract(c)),
                  () -> assertEquals(Constraint.BOT(),
                                     c.multiply(bot)),
                  () -> assertEquals(Constraint.BOT(),
                                     bot.multiply(c)),
                  () -> assertEquals(Constraint.BOT(),
                                     c.divide(bot)),
                  () -> assertEquals(Constraint.BOT(),
                                     bot.divide(c)),
                  () -> assertEquals(Constraint.BOT(),
                                     c.modulus(bot)),
                  () -> assertEquals(Constraint.BOT(),
                                     bot.modulus(c)));
    }

    @Property
    void constraintNegation(@ForAll Constraint c) {
        assertEquals(new Constraint(c.bound() * -1,
                                    c.predicate().negate()),
                     c.negate());
    }

    @Property
    boolean constraintNegationReversable(@ForAll Constraint c) {
        Constraint k = c.copy();
        return k.equals(c.negate().negate());
    }

    @Property
    void compareToBottom(@ForAll Constraint c) {
        assertAll(() -> assertEquals(-1, Constraint.BOT().compareTo(c)),
                  () -> assertEquals(+1, c.compareTo(Constraint.BOT())));
    }

    @Property
    void constraintMinWhenTop(@ForAll Constraint c) {
        assertAll(() -> assertEquals(c, Constraint.min(Constraint.TOP(), c)),
                  () -> assertEquals(c, Constraint.min(c, Constraint.TOP())));
    }

    @Property
    void constraintMinWhenBot(@ForAll Constraint c) {
        assertAll(() -> assertEquals(c, Constraint.min(Constraint.BOT(), c)),
                  () -> assertEquals(c, Constraint.min(c, Constraint.BOT())));
    }

    @Property
    void constraintMinimum(@ForAll @IntRange(min=-523288, max=523288) int a,
                           @ForAll @IntRange(min=-523288, max=523288) int b) {
        assertEquals(new Constraint(Math.min(a, b)),
                     Constraint.min(new Constraint(a),
                                    new Constraint(b)));
    }

    @Property
    void constraintMinIsBottomWhenIncomparable(@ForAll @IntRange(min=-523288, max=523288) int a,
                                               @ForAll @IntRange(min=-523288, max=523288) int b) {
        Constraint c = Constraint.min(new Constraint(a, PredicateType.Le),
                                      new Constraint(b, PredicateType.Gt));
        assertTrue(c.isBottom());
    }

    @Property
    void constraintMaxIsTop(@ForAll Constraint c) {
        assertEquals(Constraint.TOP(), Constraint.max(c, Constraint.TOP()));
        assertEquals(Constraint.TOP(), Constraint.max(Constraint.TOP(), c));
    }

    @Property
    void constraintMaxWhenBottom(@ForAll Constraint c) {
        assertAll(() -> assertEquals(c, Constraint.max(c, Constraint.BOT())),
                  () -> assertEquals(c, Constraint.max(Constraint.BOT(), c)));
    }

    @Property
    void constraintMaximum(@ForAll @IntRange(min=-523288, max=523288) int a,
                           @ForAll @IntRange(min=-523288, max=523288) int b) {
        assertEquals(new Constraint(Math.max(a, b)),
                     Constraint.max(new Constraint(a),
                                    new Constraint(b)));
    }

    @Property
    void constraintMaximumBotWhenIncomparable(@ForAll @IntRange(min=-523288, max=523288) int a,
                                              @ForAll @IntRange(min=-523288, max=523288) int b) {
        Constraint c = Constraint.max(new Constraint(a, PredicateType.Le),
                                      new Constraint(b, PredicateType.Gt));
        assertEquals(Constraint.BOT(), c);
    }

    @Property
    void constraintNarrowWithBottomIsBottom(@ForAll Constraint c) {
        assertAll(() -> assertTrue(Constraint.narrow(Constraint.BOT(), c).isBottom()),
                  () -> assertTrue(Constraint.narrow(c, Constraint.BOT()).isBottom()));
    }

    @Property
    void constraintNarrowWithTopIsC(@ForAll Constraint c) {
        assertAll(() -> assertEquals(c, Constraint.narrow(Constraint.TOP(), c)),
                  () -> assertEquals(c, Constraint.narrow(c, Constraint.TOP())));
    }
}
