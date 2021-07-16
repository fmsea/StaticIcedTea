package abstractinterp.scalar.state;

import java.util.List;
import soot.IntType;
import soot.Local;
import soot.jimple.Jimple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class Interval32BoxTest {

    private SolverWrapper solver;

    @BeforeEach
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Test
    void testInterval32BoxClone() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box newTop = new Interval32Box(top);
        Assertions.assertTrue(newTop.isTop());
    }

    @Test
    void testInterval32BoxCloneWhenNull() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box newBot = new Interval32Box(bot);
        Assertions.assertTrue(newBot.isBottom());
    }

    @Test
    void testIsBottom() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(bot.isBottom());
        Assertions.assertFalse(top.isBottom());
        Assertions.assertFalse(max.isBottom());
        Interval32Box box = new Interval32Box(1, 0);
        Assertions.assertTrue(box.isBottom());
    }

    @Test
    void testIsTop() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(top.isTop());
        Assertions.assertFalse(bot.isTop());
        Assertions.assertFalse(max.isTop());
        Interval32Box box = new Interval32Box(null, 5);
        Assertions.assertFalse(box.isTop());
        box = new Interval32Box(-5, null);
        Assertions.assertFalse(box.isTop());
    }

    @Test
    void testIsLowerBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isLowerBounded());
        Assertions.assertFalse(top.isLowerBounded());
        Assertions.assertTrue(max.isLowerBounded());
        Interval32Box box = new Interval32Box(null, 1);
        Assertions.assertFalse(box.isLowerBounded());
    }

    @Test
    void testIsUpperBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isUpperBounded());
        Assertions.assertFalse(top.isUpperBounded());
        Assertions.assertTrue(max.isUpperBounded());
        Interval32Box box = new Interval32Box(1, null);
        Assertions.assertFalse(box.isUpperBounded());
    }

    @Test
    void testIsBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isBounded());
        Assertions.assertFalse(top.isBounded());
        Assertions.assertTrue(max.isBounded());
        Interval32Box box = new Interval32Box(1, null);
        Assertions.assertFalse(box.isBounded());
        box = new Interval32Box(0, 1);
        Assertions.assertTrue(box.isBounded());
    }

    @Test
    void testIsValid() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isValid());
        Assertions.assertTrue(top.isValid());
        Assertions.assertTrue(max.isValid());
        Interval32Box box = new Interval32Box(0, 1);
        Assertions.assertTrue(box.isValid());
    }

    @Test
    void testBottomDoesNotContainPoints() {
        Assertions.assertFalse(Interval32Box.BOT().containsIntegerPoint());
    }

    @Test
    void testTopContainsIntegerPoints() {
        Assertions.assertTrue(Interval32Box.TOP().containsIntegerPoint());
    }

    @Test
    void testMaxContainsIntegerPoints() {
        Assertions.assertTrue(Interval32Box.MAX().containsIntegerPoint());
    }

    @Test
    void testUpperBoundAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box ano_bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.upperBoundAssign(ano_bot);
        Assertions.assertTrue(bot.isBottom());
        ano_bot.upperBoundAssign(top);
        Assertions.assertFalse(ano_bot.isUpperBounded());
        Assertions.assertFalse(ano_bot.isLowerBounded());
        Assertions.assertTrue(ano_bot.isTop());
        bot.upperBoundAssign(max);
        Assertions.assertTrue(bot.isUpperBounded());
        Assertions.assertTrue(bot.isLowerBounded());
        Assertions.assertEquals(max, bot);
        top.upperBoundAssign(Interval32Box.BOT());
        Assertions.assertEquals(Interval32Box.TOP(), top);
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(1, 2);
        x.upperBoundAssign(y);
        Assertions.assertEquals(0, x.lowerBound());
        Assertions.assertEquals(2, x.upperBound());
        x = new Interval32Box(0, 1);
        y = new Interval32Box(-1, 2);
        x.upperBoundAssign(y);
        Assertions.assertEquals(-1, x.lowerBound());
        Assertions.assertEquals(2, x.upperBound());
        y.upperBoundAssign(top);
        Assertions.assertEquals(Interval32Box.TOP(), y);
    }

    @Test
    void testWideningAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box a = new Interval32Box(null, 0);
        Interval32Box b = new Interval32Box(0, null);
        bot.wideningAssign(bot);
        Assertions.assertTrue(bot.isBottom());
        bot.wideningAssign(top);
        Assertions.assertTrue(bot.isTop());
        top.wideningAssign(a);
        Assertions.assertTrue(top.isTop());
        a.wideningAssign(b);
        Assertions.assertEquals(0, a.lowerBound());
        Assertions.assertEquals(0, a.upperBound());
        bot = Interval32Box.BOT();
        a = new Interval32Box(1, 10);
        bot.wideningAssign(a);
        Assertions.assertFalse(bot.isBottom());
        Assertions.assertEquals(1, bot.lowerBound());
        Assertions.assertEquals(10, bot.upperBound());
        b = new Interval32Box(1, 11);
        a.wideningAssign(b);
        Assertions.assertEquals(1, a.lowerBound());
        Assertions.assertEquals(Integer.MAX_VALUE, a.upperBound());
        b = new Interval32Box(0, 12);
        a.wideningAssign(b);
        Assertions.assertEquals(Integer.MIN_VALUE, a.lowerBound());
        Assertions.assertEquals(Integer.MAX_VALUE, a.upperBound());
    }

    @Test
    void testNegate() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.negate();
        Assertions.assertTrue(bot.isBottom());
        top.negate();
        Assertions.assertTrue(top.isTop());
        max.negate();
        Assertions.assertTrue(max.isBottom());
        Assertions.assertEquals(-1 * Integer.MAX_VALUE, max.lowerBound());
        Assertions.assertEquals(-1 * Integer.MIN_VALUE, max.upperBound());
        Interval32Box box = new Interval32Box(0, 1);
        box.negate();
        Assertions.assertEquals(-1, box.lowerBound());
        Assertions.assertEquals(0, box.upperBound());
        box = new Interval32Box(-5, 1);
        box.negate();
        Assertions.assertEquals(-1, box.lowerBound());
        Assertions.assertEquals(5, box.upperBound());
    }

    @Test
    void testIntersectionPosition() {
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = null;
        Assertions.assertEquals(-1, x.intersectionPosition(y));
        y = new Interval32Box(3, 4);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        Assertions.assertEquals(4, y.intersectionPosition(x));
        y = new Interval32Box(1, 3);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        Assertions.assertEquals(3, y.intersectionPosition(x));
        y = new Interval32Box(1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        Assertions.assertEquals(5, y.intersectionPosition(x));
        y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        Assertions.assertEquals(1, y.intersectionPosition(x));
        y = new Interval32Box(-2, -1);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        Assertions.assertEquals(0, y.intersectionPosition(x));
        y = new Interval32Box(-1, 3);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        Assertions.assertEquals(2, y.intersectionPosition(x));
    }

    @Test
    void testToString() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertEquals("⟘", bot.toString());
        Assertions.assertEquals("⟙", top.toString());
        Assertions.assertEquals("[-2147483648, 2147483647]", max.toString());
        Interval32Box box = new Interval32Box(-4, 16);
        Assertions.assertEquals("[-4, 16]", box.toString());
        box = new Interval32Box(1, 1);
        Assertions.assertEquals("1", box.toString());
    }

    @Test
    void testSingletonInterval() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isSingleton());
        Assertions.assertFalse(top.isSingleton());
        Assertions.assertFalse(max.isSingleton());
        Interval32Box box = new Interval32Box(null, 3);
        Assertions.assertFalse(box.isSingleton());
        box = new Interval32Box(3, null);
        Assertions.assertFalse(box.isSingleton());
        box = new Interval32Box(0, 3);
        Assertions.assertFalse(box.isSingleton());
        box = new Interval32Box(0, 0);
        Assertions.assertTrue(box.isSingleton());
    }

    @Test
    void testEquals() {
        Interval32Box bot = Interval32Box.BOT();
        Assertions.assertFalse(bot.equals(null));
        Assertions.assertTrue(bot.equals(Interval32Box.BOT()));
        Interval32Box top = Interval32Box.TOP();
        Assertions.assertTrue(top.equals(Interval32Box.TOP()));
        Assertions.assertFalse(top.equals(bot));
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(max.equals(Interval32Box.MAX()));
        Assertions.assertFalse(max.equals(top));
        Assertions.assertFalse(max.equals(bot));
    }

    @Test
    void testToSMTFormula() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        Assertions.assertEquals("(and (>= l0 0) (< l0 0))", bot.toSMT(l, this.solver));
        Interval32Box top = Interval32Box.TOP();
        Assertions.assertEquals("(or (>= l0 0) (< l0 0))", top.toSMT(l, this.solver));
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertEquals("(and (>= l0 (- 2147483648)) (<= l0 2147483647))",
                                max.toSMT(l, this.solver));
        Interval32Box box = new Interval32Box(5);
        Assertions.assertEquals("(= l0 5)", box.toSMT(l, this.solver));
        box = new Interval32Box(-5, 5);
        Assertions.assertEquals("(and (>= l0 (- 5)) (<= l0 5))",
                                box.toSMT(l, this.solver));
        box = new Interval32Box(null, 5);
        Assertions.assertEquals("(<= l0 5)", box.toSMT(l, this.solver));
    }

    @Test
    void testToGrimpExpr() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(null, 5);
        Assertions.assertEquals("l0 >= 0 & l0 < 0", bot.toGrimpExpr(l).toString());
        Assertions.assertEquals("l0 >= 0 | l0 < 0", top.toGrimpExpr(l).toString());
        Assertions.assertEquals(String.format("l0 >= %d & l0 <= %d",
                                              Integer.MIN_VALUE,
                                              Integer.MAX_VALUE),
                                max.toGrimpExpr(l).toString());
        Assertions.assertEquals("l0 <= 5", box.toGrimpExpr(l).toString());
        box = new Interval32Box(-5, null);
        Assertions.assertEquals("l0 >= -5", box.toGrimpExpr(l).toString());
        box = new Interval32Box(-5, 5);
        Assertions.assertEquals("l0 >= -5 & l0 <= 5", box.toGrimpExpr(l).toString());
    }

    @Test
    void testTransferConditionEqPosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionEqPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(0, 1), b);
        }
    }

    @Test
    void testTransferConditionEqPosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(-1, 1), b);
        }
    }

    @Test
    void testTransferConditionEqPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(0, 1), b);
        }
    }

    @Test
    void testTransferConditionEqPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b: actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionEqPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(-1, 1), b);
        }
    }

    @Test
    void testTransferConditionNeWhenEqual() {
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(0, 1);

        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionNeWhenNotEqual() {
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(0, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));
    }

    @Test
    void testTransferConditionLePosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 0), actual.get(0));
        Assertions.assertEquals(new Interval32Box(1, 2), actual.get(1));
    }

    @Test
    void testTransferConditionLePosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(0, 2), actual.get(1));
    }

    @Test
    void testTransferConditionLePosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-2, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));
    }

    @Test
    void testTransferConditionLePositon3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(0, 1), b);
        }
    }

    @Test
    void testTransferConditionLePosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b: actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionLePosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 2), actual.get(1));
    }

    @Test
    void testTransferConditionLtPosition0() {
        List<Interval32Box> actual;
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 0), actual.get(0));
        Assertions.assertEquals(new Interval32Box(1, 2), actual.get(1));
    }

    @Test
    void testTransferConditionLtPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(0, 2), actual.get(1));
    }

    @Test
    void testTransferConditionLtPosition2Eq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionLtPosition2Ne() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-2, 0), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));
    }

    @Test
    void testTransferConditionLtPosition2NeEq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-2, 0), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));
    }

    @Test
    void testTransferConditionLtPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(0), actual.get(0));
        Assertions.assertEquals(new Interval32Box(1), actual.get(1));

    }

    @Test
    void testTransferConditionLtPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b: actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionLtPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(0, 2), actual.get(1));
    }

    @Test
    void testTransferConditionGePosition0() {
        List<Interval32Box> actual;
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionGePosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(0, 1), b);
        }
    }

    @Test
    void testTransferConditionGePosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));
    }

    @Test
    void testTransferConditionGePosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(0, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));

    }

    @Test
    void testTransferConditionGePosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(1, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 0), actual.get(1));
    }

    @Test
    void testTransferConditionGePosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertEquals(new Interval32Box(-1, 1), b);
        }
    }

    @Test
    void testTransferConditionGtPosition0() {
        List<Interval32Box> actual;
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        Assertions.assertEquals(0, x.intersectionPosition(y));
        actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionGtPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        Assertions.assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(0), actual.get(1));
    }

    @Test
    void testTransferConditionGtPosition2Eq() {
        // position 2
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        for (Interval32Box b : actual) {
            Assertions.assertTrue(b.isBottom());
        }
    }

    @Test
    void testTransferConditionGtPosition2NeEq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 1);
        Assertions.assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-2, 1), actual.get(1));
    }

    @Test
    void testTransferConditionGtPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        Assertions.assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(0, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(1));

    }

    @Test
    void testTransferConditionGtPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        Assertions.assertEquals(4, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(1, 2), actual.get(0));
        Assertions.assertEquals(new Interval32Box(-1, 0), actual.get(1));
    }

    @Test
    void testTransferConditionGtPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        Assertions.assertEquals(5, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(new Interval32Box(-1, 1), actual.get(0));
        Assertions.assertEquals(new Interval32Box(0), actual.get(1));
    }
}
