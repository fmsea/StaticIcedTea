package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;
import soot.jimple.IntConstant;

public class ConstraintProperties {

    @Property
    void constantAdditionTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                                  @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        Assertions.assertEquals(new Constraint(x + y, PredicateType.Eq),
                                Constraint.transferBinary(IntConstant.v(x),
                                                          IntConstant.v(y),
                                                          BinaryOperator.ADDITION));
    }

    @Property
    void constantSubtractionTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                                     @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        Assertions.assertEquals(new Constraint(x - y, PredicateType.Eq),
                                Constraint.transferBinary(IntConstant.v(x),
                                                          IntConstant.v(y),
                                                          BinaryOperator.SUBTRACTION));
    }

    @Property
    void constraintMultiplicationTransfer(@ForAll @IntRange(min=-32768, max=32768) int x,
                                          @ForAll @IntRange(min=-32768, max=32768) int y) {
        Assertions.assertEquals(new Constraint(x * y, PredicateType.Eq),
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
            Assertions.assertEquals(Constraint.BOT(), actual);
        } else {
            Assertions.assertEquals(new Constraint(x / y, PredicateType.Eq),
                                    actual);
        }
    }

    @Property
    void constraintTransfer(@ForAll @IntRange(min=-536870911, max=536870911) int x,
                            @ForAll @IntRange(min=-536870911, max=536870911) int y) {
        Assertions.assertEquals(Constraint.TOP(),
                                Constraint.transferBinary(IntConstant.v(x),
                                                          IntConstant.v(y),
                                                          BinaryOperator.MODULUS));
        Assertions.assertEquals(Constraint.TOP(),
                                Constraint.transferBinary(IntConstant.v(x),
                                                          IntConstant.v(y),
                                                          BinaryOperator.INVALID));
    }

    @Property
    void bottomStaysBottom(@ForAll Constraint c) {
        Constraint bot = Constraint.BOT();
        Assertions.assertEquals(Constraint.BOT(),
                                c.add(bot));
        Assertions.assertEquals(Constraint.BOT(),
                                bot.add(c));
        Assertions.assertEquals(Constraint.BOT(),
                                c.subtract(bot));
        Assertions.assertEquals(Constraint.BOT(),
                                bot.subtract(c));
        Assertions.assertEquals(Constraint.BOT(),
                                c.multiply(bot));
        Assertions.assertEquals(Constraint.BOT(),
                                bot.multiply(c));
        Assertions.assertEquals(Constraint.BOT(),
                                c.divide(bot));
        Assertions.assertEquals(Constraint.BOT(),
                                bot.divide(c));
        Assertions.assertEquals(Constraint.BOT(),
                                c.modulus(bot));
        Assertions.assertEquals(Constraint.BOT(),
                                bot.modulus(c));
    }

    @Property
    void constraintNegation(@ForAll Constraint c) {
        Assertions.assertEquals(new Constraint(c.bound() * -1,
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
        Assertions.assertEquals(-1, Constraint.BOT().compareTo(c));
        Assertions.assertEquals(+1, c.compareTo(Constraint.BOT()));
    }

    @Property
    void constraintMinWhenTop(@ForAll Constraint c) {
        Assertions.assertEquals(c, Constraint.min(Constraint.TOP(), c));
        Assertions.assertEquals(c, Constraint.min(c, Constraint.TOP()));
    }

    @Property
    void constraintMinWhenBot(@ForAll Constraint c) {
        Assertions.assertEquals(c, Constraint.min(Constraint.BOT(), c));
        Assertions.assertEquals(c, Constraint.min(c, Constraint.BOT()));
    }

    @Property
    void constraintMinimum(@ForAll @IntRange(min=-523288, max=523288) int a,
                           @ForAll @IntRange(min=-523288, max=523288) int b) {
        Assertions.assertEquals(new Constraint(Math.min(a, b)),
                                Constraint.min(new Constraint(a),
                                               new Constraint(b)));
    }

    @Property
    void constraintMinIsBottomWhenIncomparable(@ForAll @IntRange(min=-523288, max=523288) int a,
                                               @ForAll @IntRange(min=-523288, max=523288) int b) {
        Constraint c = Constraint.min(new Constraint(a, PredicateType.Le),
                                      new Constraint(b, PredicateType.Gt));
        Assertions.assertTrue(c.isBottom());
    }

    @Property
    void constraintMaxIsTop(@ForAll Constraint c) {
        Assertions.assertEquals(Constraint.TOP(), Constraint.max(c, Constraint.TOP()));
        Assertions.assertEquals(Constraint.TOP(), Constraint.max(Constraint.TOP(), c));
    }

    @Property
    void constraintMaxWhenBottom(@ForAll Constraint c) {
        Assertions.assertEquals(c, Constraint.max(c, Constraint.BOT()));
        Assertions.assertEquals(c, Constraint.max(Constraint.BOT(), c));
    }

    @Property
    void constraintMaximum(@ForAll @IntRange(min=-523288, max=523288) int a,
                           @ForAll @IntRange(min=-423288, max=423288) int b) {
        Assertions.assertEquals(new Constraint(Math.max(a, b)),
                                Constraint.max(new Constraint(a),
                                               new Constraint(b)));
    }

    @Property
    void constraintMaximumBotWhenIncomparable(@ForAll @IntRange(min=-523288, max=523288) int a,
                                              @ForAll @IntRange(min=-523288, max=523288) int b) {
        Constraint c = Constraint.max(new Constraint(a, PredicateType.Le),
                                      new Constraint(b, PredicateType.Gt));
        Assertions.assertEquals(Constraint.BOT(), c);
    }
}
