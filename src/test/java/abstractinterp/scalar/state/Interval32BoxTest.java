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
        {
            Interval32Box bot = Interval32Box.BOT();
            bot.upperBoundAssign(Interval32Box.BOT());
            assertTrue(bot.isBottom());
        }

        {
            Interval32Box bot = Interval32Box.BOT();
            Interval32Box top = Interval32Box.TOP();
            Interval32Box r1 = Interval32Box.upperBoundAssign(bot, top);
            Interval32Box r2 = Interval32Box.upperBoundAssign(top, bot);
            assertAll(() -> assertFalse(r1.isBottom()),
                      () -> assertTrue(r1.isTop()),
                      () -> assertFalse(r2.isBottom()),
                      () -> assertTrue(r2.isTop()));
        }

        {
            Interval32Box x = new Interval32Box(0, 1);
            Interval32Box y = new Interval32Box(1, 2);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(0, r1.lowerBoundOrElse()),
                      () -> assertEquals(2, r1.upperBoundOrElse()),
                      () -> assertEquals(0, r2.lowerBoundOrElse()),
                      () -> assertEquals(2, r2.upperBoundOrElse()));
        }

        {
            Interval32Box x = new Interval32Box(0, 1);
            Interval32Box y = new Interval32Box(-1, 0);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(-1, r1.lowerBoundOrElse()),
                      () -> assertEquals(+1, r1.upperBoundOrElse()),
                      () -> assertEquals(-1, r2.lowerBoundOrElse()),
                      () -> assertEquals(+1, r2.upperBoundOrElse()));
        }

        {
            Interval32Box x = new Interval32Box(0, 1);
            Interval32Box y = new Interval32Box(-1, 2);
            Interval32Box z = new Interval32Box(-1, 2);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(z, r1),
                      () -> assertEquals(z, r2));
        }

        {
            Interval32Box x = new Interval32Box(0, null);
            Interval32Box y = new Interval32Box(1, 5);
            Interval32Box e = new Interval32Box(0, null);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(e, r1),
                      () -> assertEquals(e, r2));
        }

        {
            Interval32Box x = new Interval32Box(null, 0);
            Interval32Box y = new Interval32Box(-5, -1);
            Interval32Box e = new Interval32Box(null, 0);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(e, r1),
                      () -> assertEquals(e, r2));
        }
    }

    @Test
    void testWideningAssign() {
        {
            Interval32Box bot = Interval32Box.BOT();
            bot.wideningAssign(bot);
            Interval32Box c = Interval32Box.wideningAssign(bot, bot);
            assertTrue(c.isBottom());
        }
        {
            Interval32Box bot = Interval32Box.BOT();
            Interval32Box top = Interval32Box.TOP();
            Interval32Box c = Interval32Box.wideningAssign(bot, top);
            assertAll(() -> assertTrue(c.isBottom()),
                      () -> assertFalse(c.isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box a = new Interval32Box(null, 0);
            Interval32Box b = new Interval32Box(0, null);
            assertAll(() -> assertTrue((Interval32Box.wideningAssign(a, top)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(top, a)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(b, top)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(top, b)).isTop()));
        }

        {
            Interval32Box a = new Interval32Box(1, 10);
            Interval32Box b = new Interval32Box(2, 9);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertTrue(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()));
        }

        {
            Interval32Box a = new Interval32Box(2, 9);
            Interval32Box b = new Interval32Box(1, 10);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertTrue(c.isLowerBounded()),
                      () -> assertTrue(c.isUpperBounded()),
                      () -> assertEquals(2, c.lowerBoundOrElse()),
                      () -> assertEquals(9, c.upperBoundOrElse()));
        }

        {
            Interval32Box a = new Interval32Box(1, 5);
            Interval32Box b = new Interval32Box(0, null);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertTrue(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()),
                      () -> assertEquals(1, c.lowerBoundOrElse()));
        }

        {
            Interval32Box a = new Interval32Box(1, null);
            Interval32Box b = new Interval32Box(0, 5);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertTrue(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()),
                      () -> assertEquals(1, c.lowerBoundOrElse()));
        }

        {
            Interval32Box a = new Interval32Box(-1, 0);
            Interval32Box b = new Interval32Box(null, 0);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertTrue(c.isUpperBounded()),
                      () -> assertEquals(0, c.upperBoundOrElse()));
        }

        {
            Interval32Box a = new Interval32Box(null, 0);
            Interval32Box b = new Interval32Box(-1, 0);
            Interval32Box c = Interval32Box.wideningAssign(a, b);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertTrue(c.isUpperBounded()),
                      () -> assertEquals(0, c.upperBoundOrElse()));
        }
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
    void testTransferConditionEqWithTOP() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Eq);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Eq);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(x, rs.get(0)),
                      () -> assertEquals(x, rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Eq);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(x, rs.get(0)),
                      () -> assertEquals(x, rs.get(1)));
        }
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
    void testTransferConditionNeWhenTop() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Ne);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertTrue(rs.get(0).isBottom()),
                      () -> assertTrue(rs.get(1).isBottom()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Ne);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(top, rs.get(0)),
                      () -> assertEquals(x, rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Ne);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()),
                      () -> assertEquals(x, rs.get(0)),
                      () -> assertEquals(top, rs.get(1)));
        }
    }

    @Test
    void testTransferConditionLeWithTOP() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Le);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Le);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(new Interval32Box(null, 1), rs.get(0)),
                      () -> assertEquals(x, rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Le);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(x, rs.get(0)),
                      () -> assertEquals(new Interval32Box(null, 1), rs.get(1)));
        }
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
    void testTransferConditionLtWhenTOP() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Lt);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isBottom()),
                      () -> assertFalse(rs.get(1).isBottom()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Lt);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(new Interval32Box(null, 0), rs.get(0)),
                      () -> assertEquals(x, rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Lt);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(x, rs.get(0)),
                      () -> assertEquals(new Interval32Box(1, null), rs.get(1)));
        }
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
    void testTransferConditionGeWhenTop() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Ge);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Ge);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(new Interval32Box(0, null), rs.get(0)),
                      () -> assertEquals(new Interval32Box(0, 1), rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Ge);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(new Interval32Box(0, 1), rs.get(0)),
                      () -> assertEquals(new Interval32Box(0, 1), rs.get(1)));
        }
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
    void testTransferConditionGtWhenTop() {
        {
            Interval32Box t1 = Interval32Box.TOP();
            Interval32Box t2 = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(t1, t2, PredicateType.Gt);
            assertAll(() -> assertEquals(2, t1.intersectionPosition(t2)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isBottom()),
                      () -> assertFalse(rs.get(1).isBottom()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(top, x, PredicateType.Gt);
            assertAll(() -> assertEquals(2, top.intersectionPosition(x)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(new Interval32Box(1, null), rs.get(0)),
                      () -> assertEquals(new Interval32Box(0, 1), rs.get(1)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box x = new Interval32Box(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, top, PredicateType.Gt);
            assertAll(() -> assertEquals(5, x.intersectionPosition(top)),
                      () -> assertEquals(2, rs.size()),
                      () -> assertFalse(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isBottom()),
                      () -> assertEquals(new Interval32Box(0, 1), rs.get(0)),
                      () -> assertEquals(Interval32Box.BOT(), rs.get(1)));
        }
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
