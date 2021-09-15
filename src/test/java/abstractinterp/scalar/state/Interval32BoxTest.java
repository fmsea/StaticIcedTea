package abstractinterp.scalar.state;

import java.util.List;
import java.util.Optional;
import soot.IntType;
import soot.Local;
import soot.jimple.Jimple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertTrue(newTop.isTop());
    }

    @Test
    void testInterval32BoxCloneWhenNull() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box newBot = new Interval32Box(bot);
        assertTrue(newBot.isBottom());
    }

    @Test
    void testIsBottom() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(1, 0);
        assertAll(() -> assertTrue(bot.isBottom()),
                  () -> assertFalse(top.isBottom()),
                  () -> assertFalse(max.isBottom()),
                  () -> assertTrue(box.isBottom()));
    }

    @Test
    void testIsTop() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        assertAll(() -> assertTrue(top.isTop()),
                  () -> assertFalse(bot.isTop()),
                  () -> assertFalse(max.isTop()));
        {
            Interval32Box box = new Interval32Box(null, 5);
            assertFalse(box.isTop());
        }

        {
            Interval32Box box = new Interval32Box(-5, null);
            assertFalse(box.isTop());
        }
    }

    @Test
    void testIsLowerBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(null, 1);
        assertAll(() -> assertFalse(bot.isLowerBounded()),
                  () -> assertFalse(top.isLowerBounded()),
                  () -> assertTrue(max.isLowerBounded()),
                  () -> assertFalse(box.isLowerBounded()));
    }

    @Test
    void testIsUpperBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(1, null);
        assertAll(() -> assertFalse(bot.isUpperBounded()),
                  () -> assertFalse(top.isUpperBounded()),
                  () -> assertTrue(max.isUpperBounded()),
                  () -> assertFalse(box.isUpperBounded()));
    }

    @Test
    void testIsBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        assertAll(() -> assertFalse(bot.isBounded()),
                  () -> assertFalse(top.isBounded()),
                  () -> assertTrue(max.isBounded()));
        {
            Interval32Box box = new Interval32Box(1, null);
            assertFalse(box.isBounded());
        }
        {
            Interval32Box box = new Interval32Box(null, 1);
            assertFalse(box.isBounded());
        }
        {
            Interval32Box box = new Interval32Box(0, 1);
            assertTrue(box.isBounded());
        }
    }

    @Test
    void testIsValid() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(0, 1);
        assertAll(() -> assertFalse(bot.isValid()),
                  () -> assertTrue(top.isValid()),
                  () -> assertTrue(max.isValid()),
                  () -> assertTrue(box.isValid()));
    }

    @Test
    void testBottomDoesNotContainPoints() {
        assertFalse(Interval32Box.BOT().containsIntegerPoint());
    }

    @Test
    void testTopContainsIntegerPoints() {
        assertTrue(Interval32Box.TOP().containsIntegerPoint());
    }

    @Test
    void testMaxContainsIntegerPoints() {
        assertTrue(Interval32Box.MAX().containsIntegerPoint());
    }

    @Test
    void testUpperBoundAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box ano_bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.upperBoundAssign(ano_bot);
        assertTrue(bot.isBottom());
        ano_bot.upperBoundAssign(top);
        assertFalse(ano_bot.isUpperBounded());
        assertFalse(ano_bot.isLowerBounded());
        assertTrue(ano_bot.isTop());
        bot.upperBoundAssign(max);
        assertFalse(bot.isUpperBounded());
        assertFalse(bot.isLowerBounded());
        assertTrue(bot.isTop());
        top.upperBoundAssign(Interval32Box.BOT());
        assertEquals(Interval32Box.TOP(), top);
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(1, 2);
        x.upperBoundAssign(y);
        assertEquals(0, x.lowerBound().get());
        assertEquals(2, x.upperBound().get());
        x = new Interval32Box(0, 1);
        y = new Interval32Box(-1, 2);
        x.upperBoundAssign(y);
        assertEquals(-1, x.lowerBound().get());
        assertEquals(2, x.upperBound().get());
        y.upperBoundAssign(top);
        assertEquals(Interval32Box.TOP(), y);
    }

    @Test
    void testWideningAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box a = new Interval32Box(null, 0);
        Interval32Box b = new Interval32Box(0, null);
        bot.wideningAssign(bot);
        assertTrue(bot.isBottom());
        bot.wideningAssign(top);
        assertTrue(bot.isTop());
        top.wideningAssign(a);
        assertTrue(top.isTop());
        a.wideningAssign(b);
        assertFalse(a.isLowerBounded());
        assertFalse(a.isUpperBounded());
        bot = Interval32Box.BOT();
        a = new Interval32Box(1, 10);
        bot.wideningAssign(a);
        assertFalse(bot.isBottom());
        assertFalse(bot.isLowerBounded());
        assertFalse(bot.isUpperBounded());
        b = new Interval32Box(1, 11);
        a.wideningAssign(b);
        assertEquals(1, a.lowerBound().get());
        assertEquals(Integer.MAX_VALUE, a.upperBound().get());
        b = new Interval32Box(0, 12);
        a.wideningAssign(b);
        assertEquals(Integer.MIN_VALUE, a.lowerBound().get());
        assertEquals(Integer.MAX_VALUE, a.upperBound().get());
    }

    @Test
    void testNegate() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.negate();
        assertTrue(bot.isBottom());
        top.negate();
        assertTrue(top.isTop());
        max.negate();
        assertTrue(max.isBottom());
        assertEquals(-1 * Integer.MAX_VALUE, max.lowerBound().get());
        assertEquals(-1 * Integer.MIN_VALUE, max.upperBound().get());
        Interval32Box box = new Interval32Box(0, 1);
        box.negate();
        assertEquals(-1, box.lowerBound().get());
        assertEquals(0, box.upperBound().get());
        box = new Interval32Box(-5, 1);
        box.negate();
        assertEquals(-1, box.lowerBound().get());
        assertEquals(5, box.upperBound().get());
    }

    @Test
    void testIntersectionPosition() {
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = null;
        assertEquals(-1, x.intersectionPosition(y));
        y = new Interval32Box(3, 4);
        assertEquals(0, x.intersectionPosition(y));
        assertEquals(4, y.intersectionPosition(x));
        y = new Interval32Box(1, 3);
        assertEquals(1, x.intersectionPosition(y));
        assertEquals(3, y.intersectionPosition(x));
        y = new Interval32Box(1, 1);
        assertEquals(2, x.intersectionPosition(y));
        assertEquals(5, y.intersectionPosition(x));
        y = new Interval32Box(-1, 1);
        assertEquals(3, x.intersectionPosition(y));
        assertEquals(1, y.intersectionPosition(x));
        y = new Interval32Box(-2, -1);
        assertEquals(4, x.intersectionPosition(y));
        assertEquals(0, y.intersectionPosition(x));
        y = new Interval32Box(-1, 3);
        assertEquals(5, x.intersectionPosition(y));
        assertEquals(2, y.intersectionPosition(x));
    }

    @Test
    void testIntersectionPositionWhenUnbounded() {
        Interval32Box x = new Interval32Box(null, 19);
        Interval32Box y = new Interval32Box(20);
        assertEquals(0, x.intersectionPosition(y));
        assertEquals(4, y.intersectionPosition(x));
        y = new Interval32Box(20, null);
        assertEquals(0, x.intersectionPosition(y));
        assertEquals(4, y.intersectionPosition(x));
        x = new Interval32Box(1, null);
        y = new Interval32Box(null, 2);
        assertEquals(3, x.intersectionPosition(y));
        assertEquals(1, y.intersectionPosition(x));
        x = new Interval32Box(2, null);
        assertEquals(3, x.intersectionPosition(y));
        assertEquals(1, y.intersectionPosition(x));
        x = Interval32Box.TOP();
        y = new Interval32Box(2);
        assertEquals(2, x.intersectionPosition(y));
        assertEquals(5, y.intersectionPosition(x));
        x = new Interval32Box(null, 3);
        assertEquals(2, x.intersectionPosition(y));
        assertEquals(5, y.intersectionPosition(x));
    }

    @Test
    void testToString() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        assertEquals("⟘", bot.toString());
        assertEquals("⟙", top.toString());
        assertEquals("[-2147483648, 2147483647]", max.toString());
        Interval32Box box = new Interval32Box(-4, 16);
        assertEquals("[-4, 16]", box.toString());
        box = new Interval32Box(1, 1);
        assertEquals("1", box.toString());
        box = new Interval32Box(null, 1);
        assertEquals("(-∞, 1]", box.toString());
        box = new Interval32Box(1, null);
        assertEquals("[1, ∞)", box.toString());
        box = new Interval32Box(Optional.empty(), Optional.empty());
        assertEquals("⟙", box.toString());
    }

    @Test
    void testSingletonInterval() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        assertFalse(bot.isSingleton());
        assertFalse(top.isSingleton());
        assertFalse(max.isSingleton());
        Interval32Box box = new Interval32Box(null, 3);
        assertFalse(box.isSingleton());
        box = new Interval32Box(3, null);
        assertFalse(box.isSingleton());
        box = new Interval32Box(0, 3);
        assertFalse(box.isSingleton());
        box = new Interval32Box(0, 0);
        assertTrue(box.isSingleton());
    }

    @Test
    void testEquals() {
        Interval32Box bot = Interval32Box.BOT();
        assertFalse(bot.equals(null));
        assertTrue(bot.equals(Interval32Box.BOT()));
        Interval32Box top = Interval32Box.TOP();
        assertTrue(top.equals(Interval32Box.TOP()));
        assertFalse(top.equals(bot));
        Interval32Box max = Interval32Box.MAX();
        assertTrue(max.equals(Interval32Box.MAX()));
        assertFalse(max.equals(top));
        assertFalse(max.equals(bot));
    }

    @Test
    void testToSMTFormula() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        assertEquals("(and (>= l0 0) (< l0 0))", bot.toSMT(l, this.solver));
        Interval32Box top = Interval32Box.TOP();
        assertEquals("(or (>= l0 0) (< l0 0))", top.toSMT(l, this.solver));
        Interval32Box max = Interval32Box.MAX();
        assertEquals("(and (>= l0 (- 2147483648)) (<= l0 2147483647))",
                     max.toSMT(l, this.solver));
        Interval32Box box = new Interval32Box(5);
        assertEquals("(= l0 5)", box.toSMT(l, this.solver));
        box = new Interval32Box(-5, 5);
        assertEquals("(and (>= l0 (- 5)) (<= l0 5))",
                     box.toSMT(l, this.solver));
        box = new Interval32Box(null, 5);
        assertEquals("(<= l0 5)", box.toSMT(l, this.solver));
    }

    @Test
    void testToGrimpExpr() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = new Interval32Box(null, 5);
        assertEquals("l0 >= 0 & l0 < 0", bot.toGrimpExpr(l).toString());
        assertEquals("l0 >= 0 | l0 < 0", top.toGrimpExpr(l).toString());
        assertEquals(String.format("l0 >= %d & l0 <= %d",
                                   Integer.MIN_VALUE,
                                   Integer.MAX_VALUE),
                     max.toGrimpExpr(l).toString());
        assertEquals("l0 <= 5", box.toGrimpExpr(l).toString());
        box = new Interval32Box(-5, null);
        assertEquals("l0 >= -5", box.toGrimpExpr(l).toString());
        box = new Interval32Box(-5, 5);
        assertEquals("l0 >= -5 & l0 <= 5", box.toGrimpExpr(l).toString());
    }

    @Test
    void testTransferConditionEqPosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(0, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionEqPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(1, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionEqPosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionEqPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(3, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionEqPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(4, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionEqPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertAll(() -> assertEquals(5, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionNeWhenEqual() {
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(0, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(0).isBottom()));
    }

    @Test
    void testTransferConditionNeWhenNotEqual() {
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionLePosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        assertEquals(0, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 0), actual.get(0)),
                  () -> assertEquals(new Interval32Box(1, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionLePosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        assertEquals(1, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionLePosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        assertEquals(2, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-2, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionLePositon3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        assertEquals(3, x.intersectionPosition(y));
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionLePosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(4, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionLePosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertAll(() -> assertEquals(5, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionLtPosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(0, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 0), actual.get(0)),
                  () -> assertEquals(new Interval32Box(1, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionLtPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(1, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionLtPosition2Eq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionLtPosition2Ne() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-2, 0), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionLtPosition2NeEq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-2, 0), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionLtPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(3, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0), actual.get(0)),
                  () -> assertEquals(new Interval32Box(1), actual.get(1)));

    }

    @Test
    void testTransferConditionLtPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(4, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionLtPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertAll(() -> assertEquals(5, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 2), actual.get(1)));
    }

    @Test
    void testTransferConditionGePosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(0, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionGePosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(1, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionGePosition2() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionGePosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(3, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));

    }

    @Test
    void testTransferConditionGePosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(4, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(1, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 0), actual.get(1)));
    }

    @Test
    void testTransferConditionGePosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertAll(() -> assertEquals(5, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionGtPosition0() {
        // position 0
        Interval32Box x = new Interval32Box(-1, 0);
        Interval32Box y = new Interval32Box(1, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(0, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionGtPosition1() {
        // position 1
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(0, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(1, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0), actual.get(1)));
    }

    @Test
    void testTransferConditionGtPosition2Eq() {
        // position 2
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertTrue(actual.get(0).isBottom()),
                  () -> assertTrue(actual.get(1).isBottom()));
    }

    @Test
    void testTransferConditionGtPosition2NeEq() {
        // position 2
        Interval32Box x = new Interval32Box(-2, 2);
        Interval32Box y = new Interval32Box(-2, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(2, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-2, 1), actual.get(1)));
    }

    @Test
    void testTransferConditionGtPosition3() {
        // position 3
        Interval32Box x = new Interval32Box(0, 2);
        Interval32Box y = new Interval32Box(-1, 1);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(3, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(0, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(1)));

    }

    @Test
    void testTransferConditionGtPosition4() {
        // position 4
        Interval32Box x = new Interval32Box(1, 2);
        Interval32Box y = new Interval32Box(-1, 0);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(4, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(1, 2), actual.get(0)),
                  () -> assertEquals(new Interval32Box(-1, 0), actual.get(1)));
    }

    @Test
    void testTransferConditionGtPosition5() {
        // position 5
        Interval32Box x = new Interval32Box(-1, 1);
        Interval32Box y = new Interval32Box(-2, 2);
        List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertAll(() -> assertEquals(5, x.intersectionPosition(y)),
                  () -> assertEquals(2, actual.size()),
                  () -> assertEquals(new Interval32Box(-1, 1), actual.get(0)),
                  () -> assertEquals(new Interval32Box(0), actual.get(1)));
    }

    @Test
    void testTransferAddition() {
        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(0, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(0, null);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(new Interval32Box(0, null),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(new Interval32Box(0, null),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(null, 1);
            assertAll(() -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(0, null);
            Interval32Box y = new Interval32Box(1, null);
            assertAll(() -> assertEquals(new Interval32Box(1, null),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(new Interval32Box(1, null),
                                         Interval32Box.add(y, x)));
        }
    }

    @Test
    void testTransferSubtraction() {
        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(0, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(new Interval32Box(null, 0),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(new Interval32Box(0, null),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(0, null);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(new Interval32Box(-1, null),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(null, 1);
            assertAll(() -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(new Interval32Box(null, 1),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(0, null);
            Interval32Box y = new Interval32Box(1, null);
            assertAll(() -> assertEquals(new Interval32Box(1, null),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(new Interval32Box(1, null),
                                         Interval32Box.subtract(y, x)));
        }
    }

    @Test
    void testTransferMultiplication() {
        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(0, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 1);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(1, null);
            Interval32Box y = new Interval32Box(0, 1);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }
    }

    @Test
    void testTransferDivision() {
        {
            Interval32Box x = new Interval32Box(null, 1);
            Interval32Box y = new Interval32Box(1, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(null, 1);
            Interval32Box y = new Interval32Box(1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(1, null);
            Interval32Box y = new Interval32Box(1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(0, 2);
            Interval32Box y = new Interval32Box(1, 2);
            assertAll(() -> assertEquals(new Interval32Box(0, 2),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(new Interval32Box(1, null),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(-2, 0);
            Interval32Box y = new Interval32Box(1, 2);
            assertAll(() -> assertEquals(new Interval32Box(-2, 0),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(new Interval32Box(null, 2),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(1, 2);
            Interval32Box y = new Interval32Box(-1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(new Interval32Box(-1, 2),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = new Interval32Box(Integer.MAX_VALUE);
            Interval32Box y = Interval32Box.MAX();
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(new Interval32Box(-1, 1),
                                         Interval32Box.divide(y, x)));
        }
    }
}
