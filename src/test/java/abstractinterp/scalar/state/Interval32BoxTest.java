package abstractinterp.scalar.state;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import soot.IntType;
import soot.Local;
import soot.jimple.Jimple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        Interval32Box newTop = Interval32Box.of(top);
        assertTrue(newTop.isTop());
    }

    @Test
    void testInterval32BoxCloneWhenNull() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box newBot = Interval32Box.of(bot);
        assertTrue(newBot.isBottom());
    }

    @Test
    void testIsBottom() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = Interval32Box.of(1, 0);
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
            Interval32Box box = Interval32Box.of(null, 5);
            assertFalse(box.isTop());
        }

        {
            Interval32Box box = Interval32Box.of(-5, null);
            assertFalse(box.isTop());
        }
    }

    @Test
    void testIsLowerBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = Interval32Box.of(null, 1);
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
        Interval32Box box = Interval32Box.of(1, null);
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
            Interval32Box box = Interval32Box.of(1, null);
            assertFalse(box.isBounded());
        }
        {
            Interval32Box box = Interval32Box.of(null, 1);
            assertFalse(box.isBounded());
        }
        {
            Interval32Box box = Interval32Box.of(0, 1);
            assertTrue(box.isBounded());
        }
    }

    @Test
    void testIsValid() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = Interval32Box.of(0, 1);
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
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(0, r1.lowerBoundOrElse()),
                      () -> assertEquals(2, r1.upperBoundOrElse()),
                      () -> assertEquals(0, r2.lowerBoundOrElse()),
                      () -> assertEquals(2, r2.upperBoundOrElse()));
        }

        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(-1, 0);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(-1, r1.lowerBoundOrElse()),
                      () -> assertEquals(+1, r1.upperBoundOrElse()),
                      () -> assertEquals(-1, r2.lowerBoundOrElse()),
                      () -> assertEquals(+1, r2.upperBoundOrElse()));
        }

        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(-1, 2);
            Interval32Box z = Interval32Box.of(-1, 2);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(z, r1),
                      () -> assertEquals(z, r2));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(1, 5);
            Interval32Box e = Interval32Box.of(0, null);
            Interval32Box r1 = Interval32Box.upperBoundAssign(x, y);
            Interval32Box r2 = Interval32Box.upperBoundAssign(y, x);
            assertAll(() -> assertEquals(e, r1),
                      () -> assertEquals(e, r2));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(-5, -1);
            Interval32Box e = Interval32Box.of(null, 0);
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
            assertAll(() -> assertFalse(c.isBottom()),
                      () -> assertTrue(c.isTop()));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box a = Interval32Box.of(null, 0);
            Interval32Box b = Interval32Box.of(0, null);
            assertAll(() -> assertTrue((Interval32Box.wideningAssign(a, top)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(top, a)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(b, top)).isTop()),
                      () -> assertTrue((Interval32Box.wideningAssign(top, b)).isTop()));
        }

        {
            Interval32Box m = Interval32Box.of(1, 10);
            Interval32Box n = Interval32Box.of(2, 9);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertEquals(Interval32Box.of(1, 10), c));
        }

        {
            Interval32Box m = Interval32Box.of(2, 9);
            Interval32Box n = Interval32Box.of(1, 10);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertTrue(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()),
                      () -> assertEquals(Interval32Box.TOP(), c));
        }

        {
            Interval32Box m = Interval32Box.of(0, null);
            Interval32Box n = Interval32Box.of(1, 5);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertTrue(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()),
                      () -> assertEquals(0, c.lowerBoundOrElse()));
        }

        {
            Interval32Box m = Interval32Box.of(0, 5);
            Interval32Box n = Interval32Box.of(1, null);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertTrue(c.isLowerBounded()),
                      () -> assertFalse(c.isUpperBounded()),
                      () -> assertEquals(0, c.lowerBoundOrElse()));
        }

        {
            Interval32Box m = Interval32Box.of(null, 0);
            Interval32Box n = Interval32Box.of(-1, 0);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertTrue(c.isUpperBounded()),
                      () -> assertEquals(0, c.upperBoundOrElse()));
        }

        {
            Interval32Box m = Interval32Box.of(-1, 0);
            Interval32Box n = Interval32Box.of(null, 0);
            Interval32Box c = Interval32Box.wideningAssign(m, n);
            assertAll(() -> assertFalse(c.isTop()),
                      () -> assertFalse(c.isLowerBounded()),
                      () -> assertTrue(c.isUpperBounded()),
                      () -> assertEquals(0, c.upperBoundOrElse()));
        }
    }

    @Test
    void testIsSubset() {
        {
            Interval32Box a = Interval32Box.of(0, 1);
            assertTrue(a.isSubset(a));
        }

        {
            Interval32Box a = Interval32Box.of(0, 1);
            Interval32Box top = Interval32Box.TOP();
            assertAll(() -> assertTrue(a.isSubset(top)),
                      () -> assertFalse(top.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(0, 1);
            Interval32Box b = Interval32Box.of(0, 2);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(0, 1);
            Interval32Box b = Interval32Box.of(-1, 1);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(0, 1);
            Interval32Box b = Interval32Box.of(null, 1);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(0, 1);
            Interval32Box b = Interval32Box.of(0, null);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(null, 1);
            Interval32Box b = Interval32Box.of(null, 2);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
        }

        {
            Interval32Box a = Interval32Box.of(0, null);
            Interval32Box b = Interval32Box.of(-1, null);
            assertAll(() -> assertTrue(a.isSubset(b)),
                      () -> assertFalse(b.isSubset(a)));
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
        Interval32Box box = Interval32Box.of(0, 1);
        box.negate();
        assertEquals(-1, box.lowerBound().get());
        assertEquals(0, box.upperBound().get());
        box = Interval32Box.of(-5, 1);
        box.negate();
        assertEquals(-1, box.lowerBound().get());
        assertEquals(5, box.upperBound().get());
    }

    @Test
    void testToString() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        assertEquals("⟘", bot.toString());
        assertEquals("⟙", top.toString());
        assertEquals("[-2147483648, 2147483647]", max.toString());
        Interval32Box box = Interval32Box.of(-4, 16);
        assertEquals("[-4, 16]", box.toString());
        box = Interval32Box.of(1, 1);
        assertEquals("1", box.toString());
        box = Interval32Box.of(null, 1);
        assertEquals("(-∞, 1]", box.toString());
        box = Interval32Box.of(1, null);
        assertEquals("[1, ∞)", box.toString());
        box = Interval32Box.of(Optional.empty(), Optional.empty());
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
        Interval32Box box = Interval32Box.of(null, 3);
        assertFalse(box.isSingleton());
        box = Interval32Box.of(3, null);
        assertFalse(box.isSingleton());
        box = Interval32Box.of(0, 3);
        assertFalse(box.isSingleton());
        box = Interval32Box.of(0, 0);
        assertTrue(box.isSingleton());
    }

    @Test
    void testEquals() {
        {
            Interval32Box bot = Interval32Box.BOT();
            assertAll(() -> assertFalse(bot.equals(null)),
                      () -> assertTrue(bot.equals(bot)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box bot = Interval32Box.BOT();
            assertAll(() -> assertTrue(top.equals(top)),
                      () -> assertFalse(bot.equals(top)),
                      () -> assertFalse(top.equals(bot)));
        }

        {
            Interval32Box top = Interval32Box.TOP();
            Interval32Box a = Interval32Box.of(null, 0);
            Interval32Box b = Interval32Box.of(0, null);
            Interval32Box c = Interval32Box.of(0, 1);
            assertAll(() -> assertTrue(a.equals(a)),
                      () -> assertTrue(b.equals(b)),
                      () -> assertTrue(c.equals(c)),
                      () -> assertFalse(top.equals(a)),
                      () -> assertFalse(top.equals(b)),
                      () -> assertFalse(top.equals(c)),
                      () -> assertFalse(a.equals(top)),
                      () -> assertFalse(a.equals(b)),
                      () -> assertFalse(a.equals(c)),
                      () -> assertFalse(b.equals(top)),
                      () -> assertFalse(b.equals(a)),
                      () -> assertFalse(b.equals(c)),
                      () -> assertFalse(c.equals(top)),
                      () -> assertFalse(c.equals(a)),
                      () -> assertFalse(c.equals(b)));
        }
    }

    @Test
    void testToSMTFormula() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        assertEquals("(= 0 1)", bot.toSMT(l, this.solver));
        Interval32Box top = Interval32Box.TOP();
        assertEquals("(or (>= l0 0) (< l0 0))", top.toSMT(l, this.solver));
        Interval32Box max = Interval32Box.MAX();
        assertEquals("(and (>= l0 (- 2147483648)) (<= l0 2147483647))",
                     max.toSMT(l, this.solver));
        Interval32Box box = Interval32Box.of(5);
        assertEquals("(= l0 5)", box.toSMT(l, this.solver));
        box = Interval32Box.of(-5, 5);
        assertEquals("(and (>= l0 (- 5)) (<= l0 5))",
                     box.toSMT(l, this.solver));
        box = Interval32Box.of(null, 5);
        assertEquals("(<= l0 5)", box.toSMT(l, this.solver));
    }

    @Test
    void testToGrimpExpr() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Interval32Box box = Interval32Box.of(null, 5);
        assertEquals("0 == 1", bot.toGrimpExpr(l).toString());
        assertEquals("l0 >= 0 | l0 < 0", top.toGrimpExpr(l).toString());
        assertEquals(String.format("l0 >= %d & l0 <= %d",
                                   Integer.MIN_VALUE,
                                   Integer.MAX_VALUE),
                     max.toGrimpExpr(l).toString());
        assertEquals("l0 <= 5", box.toGrimpExpr(l).toString());
        box = Interval32Box.of(-5, null);
        assertEquals("l0 >= -5", box.toGrimpExpr(l).toString());
        box = Interval32Box.of(-5, 5);
        assertEquals("l0 >= -5 & l0 <= 5", box.toGrimpExpr(l).toString());
    }

    @Test
    void testTransferConditionEq() {
        {
            Interval32Box x = Interval32Box.of(1, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 2);
            Interval32Box y = Interval32Box.of(1, 1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(1, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(-1, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(1, 2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(-1, 1);
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(-1, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(-1, 1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(null, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.of(2, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
            assertAll(() -> assertEquals(Interval32Box.BOT(), result.get(0)),
                      () -> assertEquals(Interval32Box.BOT(), result.get(1)));
        }
    }

    @Test
    void testTransferConditionNe() {
        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertEquals(Interval32Box.of(0, 1),
                                         actual.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1),
                                         actual.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(-1, 1);
            List<Interval32Box> actual = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertEquals(2, actual.size()),
                      () -> assertEquals(Interval32Box.of(0, 1), actual.get(0)),
                      () -> assertEquals(Interval32Box.of(-1, 1), actual.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(null, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertEquals(Interval32Box.of(1, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertTrue(result.get(0).isTop()),
                      () -> assertTrue(result.get(1).isTop()));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
            assertAll(() -> assertTrue(result.get(0).isTop()),
                      () -> assertEquals(Interval32Box.of(0, 1), result.get(1)));
        }
    }

    @Test
    void testTransferConditionLe() {
        {
            Interval32Box x = Interval32Box.of(1, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 2);
            Interval32Box y = Interval32Box.of(1, 1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(1, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(-1, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(-1, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(null, 2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(-1, 1);
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(-1, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(-1, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(null, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(1, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 3);
            Interval32Box y = Interval32Box.of(2, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, 4), result.get(1)));
        }
    }

    @Test
    void testTransferConditionLt() {
        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(2, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 3);
            Interval32Box y = Interval32Box.of(2, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, 4), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 3);
            Interval32Box y = Interval32Box.of(null, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, 4), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(null, 3);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(null, 2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 3);
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(1, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(null, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 4), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.TOP(), result.get(0)),
                      () -> assertEquals(Interval32Box.TOP(), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 3);
            Interval32Box y = Interval32Box.of(0, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(Integer.MIN_VALUE, 0);
            Interval32Box y = Interval32Box.of(Integer.MIN_VALUE);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
            assertAll(() -> assertTrue(result.get(0).isBottom()),
                      () -> assertTrue(result.get(1).isBottom()));
        }
    }

    @Test
    void testTransferConditionGe() {
        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(Interval32Box.of(0, null), rs.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), rs.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(Interval32Box.of(0, 1), rs.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 1), rs.get(1)));
        }
        {
            Interval32Box x = Interval32Box.of(-1);
            Interval32Box y = Interval32Box.of(-1, 1855923974);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(-1),
                                         result.get(0)),
                      () -> assertEquals(Interval32Box.of(-1),
                                         result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(2, 3);
            Interval32Box y = Interval32Box.of(1, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(2, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0);
            Interval32Box y = Interval32Box.of(1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertTrue(result.get(0).isBottom()),
                      () -> assertTrue(result.get(1).isBottom()));
        }

        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(0);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(null, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(null, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 4);
            Interval32Box y = Interval32Box.of(null, 3);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(null, 4), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(2, null);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(2, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(1, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(1, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 2);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(1, 2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(3, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 2);
            Interval32Box y = Interval32Box.of(3, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertTrue(result.get(0).isBottom()),
                      () -> assertTrue(result.get(1).isBottom()));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
            assertAll(() -> assertEquals(Interval32Box.of(0, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 3), result.get(1)));
        }
    }

    @Test
    void testTransferConditionGt() {
        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.TOP();
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertFalse(rs.get(0).isBottom()),
                      () -> assertFalse(rs.get(1).isBottom()),
                      () -> assertTrue(rs.get(0).isTop()),
                      () -> assertTrue(rs.get(1).isTop()));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertFalse(rs.get(0).isTop()),
                      () -> assertFalse(rs.get(1).isTop()),
                      () -> assertEquals(Interval32Box.of(1, null), rs.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), rs.get(1)));
        }

        {
            Interval32Box x = Interval32Box.TOP();
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> rs = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertFalse(rs.get(0).isTop()),
                      () -> assertEquals(Interval32Box.of(1, null), rs.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), rs.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(-1);
            Interval32Box y = Interval32Box.of(-2, 1855923974);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(-1),
                                         result.get(0)),
                      () -> assertEquals(Interval32Box.of(-2),
                                         result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, 2);
            Interval32Box y = Interval32Box.of(1, 2);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(2, 3);
            Interval32Box y = Interval32Box.of(1, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(2, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(2);
            Interval32Box y = Interval32Box.of(0, 1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0);
            Interval32Box y = Interval32Box.of(1);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertTrue(result.get(0).isBottom()),
                      () -> assertTrue(result.get(1).isBottom()));
        }

        {
            Interval32Box x = Interval32Box.of(1);
            Interval32Box y = Interval32Box.of(0);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(1), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(null, 4);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(null, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 2), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 4);
            Interval32Box y = Interval32Box.of(null, 3);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(null, 4), result.get(0)),
                      () -> assertEquals(Interval32Box.of(null, 3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(2, null);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(2, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(1, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(2, null), result.get(0)),
                      () -> assertEquals(Interval32Box.of(1, null), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 2);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(1, 2), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 1), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 4);
            Interval32Box y = Interval32Box.of(3, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(4), result.get(0)),
                      () -> assertEquals(Interval32Box.of(3), result.get(1)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(3, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertTrue(result.get(0).isBottom()),
                      () -> assertTrue(result.get(1).isBottom()));
        }

        {
            Interval32Box x = Interval32Box.of(null, 3);
            Interval32Box y = Interval32Box.of(0, null);
            List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), result.get(0)),
                      () -> assertEquals(Interval32Box.of(0, 2), result.get(1)));
        }
    }

    @Test
    void testTransferAddition() {
        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(0, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.of(null, 1),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.of(0, null),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.of(0, null),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(null, 1);
            assertAll(() -> assertEquals(Interval32Box.of(null, 1),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.of(null, 1),
                                         Interval32Box.add(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(1, null);
            assertAll(() -> assertEquals(Interval32Box.of(1, null),
                                         Interval32Box.add(x, y)),
                      () -> assertEquals(Interval32Box.of(1, null),
                                         Interval32Box.add(y, x)));
        }
    }

    @Test
    void testTransferSubtraction() {
        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(0, null);
            assertAll(() -> assertEquals(Interval32Box.of(null, 0),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.of(0, null),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.of(null, 0),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.of(0, null),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.of(-1, null),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.of(null, 1),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(null, 1);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(0, null);
            Interval32Box y = Interval32Box.of(1, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.subtract(y, x)));
        }
    }

    @Test
    void testTransferMultiplication() {
        {
            Interval32Box x = Interval32Box.of(null, 0);
            Interval32Box y = Interval32Box.of(0, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(0, 1);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.multiply(y, x)));
        }
    }

    @Test
    void testTransferDivision() {
        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(1, null);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(null, 1);
            Interval32Box y = Interval32Box.of(1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(1, null);
            Interval32Box y = Interval32Box.of(1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(0, 2);
            Interval32Box y = Interval32Box.of(1, 2);
            assertAll(() -> assertEquals(Interval32Box.of(0, 2),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.of(0, null),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(-2, 0);
            Interval32Box y = Interval32Box.of(1, 2);
            assertAll(() -> assertEquals(Interval32Box.of(-2, 0),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.of(null, 0),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(1, 2);
            Interval32Box y = Interval32Box.of(-1, 2);
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.of(-1, 2),
                                         Interval32Box.divide(y, x)));
        }

        {
            Interval32Box x = Interval32Box.of(Integer.MAX_VALUE);
            Interval32Box y = Interval32Box.MAX();
            assertAll(() -> assertEquals(Interval32Box.TOP(),
                                         Interval32Box.divide(x, y)),
                      () -> assertEquals(Interval32Box.of(-1, 1),
                                         Interval32Box.divide(y, x)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideIntersectionIntervals")
    void testIntersection(Interval32Box a, Interval32Box b, Interval32Box expected) {
        assertEquals(expected, Interval32Box.intersection(a, b));
    }

    private static Stream<Arguments> provideIntersectionIntervals() {
        return Stream.of(Arguments.arguments(Interval32Box.TOP(),
                                             Interval32Box.of(0),
                                             Interval32Box.of(0)),
                         Arguments.arguments(Interval32Box.BOT(),
                                             Interval32Box.of(0),
                                             Interval32Box.BOT()),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.of(1),
                                             Interval32Box.BOT()),
                         Arguments.arguments(Interval32Box.of(1, 2),
                                             Interval32Box.of(0, 3),
                                             Interval32Box.of(1, 2)),
                         Arguments.arguments(Interval32Box.of(0, 1),
                                             Interval32Box.of(1, 2),
                                             Interval32Box.of(1)),
                         Arguments.arguments(Interval32Box.of(-1, 0),
                                             Interval32Box.of(-1, 2),
                                             Interval32Box.of(-1, 0)),
                         Arguments.arguments(Interval32Box.of(null, 1),
                                             Interval32Box.of(1, null),
                                             Interval32Box.of(1)),
                         Arguments.arguments(Interval32Box.of(1, null),
                                             Interval32Box.of(null, 0),
                                             Interval32Box.BOT()),
                         Arguments.arguments(Interval32Box.of(null, 2),
                                             Interval32Box.of(-1, null),
                                             Interval32Box.of(-1, 2)),
                         Arguments.arguments(Interval32Box.of(null, 1),
                                             Interval32Box.of(null, 5),
                                             Interval32Box.of(null, 1)),
                         Arguments.arguments(Interval32Box.of(-5, null),
                                             Interval32Box.of(-10, null),
                                             Interval32Box.of(-5, null)));
    }

    @ParameterizedTest
    @MethodSource("provideInterleaveIntervals")
    void testInterleaveIntervals(Interval32Box a, Interval32Box b, Set<Interval32Box> expected) {
        assertEquals(expected, Interval32Box.interleave(a, b));
    }

    private static Stream<Arguments> provideInterleaveIntervals() {
        return Stream.of(Arguments.arguments(Interval32Box.TOP(),
                                             Interval32Box.TOP(),
                                             Set.of(Interval32Box.TOP())),
                         Arguments.arguments(Interval32Box.of(Integer.MIN_VALUE, -1),
                                             Interval32Box.of(0, Integer.MAX_VALUE),
                                             Set.of(Interval32Box.of(Integer.MIN_VALUE, -1),
                                                    Interval32Box.of(0, Integer.MAX_VALUE))),
                         Arguments.arguments(Interval32Box.TOP(),
                                             Interval32Box.of(0),
                                             Set.of(Interval32Box.of(null, -1),
                                                    Interval32Box.of(0),
                                                    Interval32Box.of(1, null))),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.TOP(),
                                             Set.of(Interval32Box.of(null, -1),
                                                    Interval32Box.of(0),
                                                    Interval32Box.of(1, null))),
                         Arguments.arguments(Interval32Box.of(null, 0),
                                             Interval32Box.of(0, null),
                                             Set.of(Interval32Box.of(null, -1),
                                                    Interval32Box.of(0),
                                                    Interval32Box.of(1, null))),
                         Arguments.arguments(Interval32Box.of(null, 0),
                                             Interval32Box.of(1, null),
                                             Set.of(Interval32Box.of(null, 0),
                                                    Interval32Box.of(1, null))),
                         Arguments.arguments(Interval32Box.of(null, 0),
                                             Interval32Box.of(0, null),
                                             Set.of(Interval32Box.of(null, -1),
                                                    Interval32Box.of(0),
                                                    Interval32Box.of(1, null))),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.of(1),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1))),
                         Arguments.arguments(Interval32Box.of(-1, 0),
                                             Interval32Box.of(0, 1),
                                             Set.of(Interval32Box.of(-1),
                                                    Interval32Box.of(0),
                                                    Interval32Box.of(1))),
                         Arguments.arguments(Interval32Box.of(-1, 2),
                                             Interval32Box.of(-1, 1),
                                             Set.of(Interval32Box.of(-1, 1),
                                                    Interval32Box.of(2))),
                         Arguments.arguments(Interval32Box.of(-1, 1),
                                             Interval32Box.of(-1, 2),
                                             Set.of(Interval32Box.of(-1, 1),
                                                    Interval32Box.of(2))),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.of(0, 1),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1))),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.of(0, 2),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 2))),
                         Arguments.arguments(Interval32Box.of(1, 2),
                                             Interval32Box.of(0, 3),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 2),
                                                    Interval32Box.of(3))),
                         Arguments.arguments(Interval32Box.of(0, 3),
                                             Interval32Box.of(1, 2),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 2),
                                                    Interval32Box.of(3))),
                         Arguments.arguments(Interval32Box.of(0, 1),
                                             Interval32Box.of(1, 7),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1),
                                                    Interval32Box.of(2, 7))),
                         Arguments.arguments(Interval32Box.of(0, 10),
                                             Interval32Box.of(1, 10),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 10))),
                         Arguments.arguments(Interval32Box.of(1, 10),
                                             Interval32Box.of(0, 10),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 10))),
                         Arguments.arguments(Interval32Box.of(0, 135158630),
                                             Interval32Box.of(0),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 135158630))),
                         Arguments.arguments(Interval32Box.of(0),
                                             Interval32Box.of(0, 135158630),
                                             Set.of(Interval32Box.of(0),
                                                    Interval32Box.of(1, 135158630))),
                         Arguments.arguments(Interval32Box.of(1659486547),
                                             Interval32Box.of(-2147483640, 1659486547),
                                             Set.of(Interval32Box.of(-2147483640, 1659486546),
                                                    Interval32Box.of(1659486547))),
                         Arguments.arguments(Interval32Box.of(55, 81915457),
                                             Interval32Box.of(-1, 0),
                                             Set.of(Interval32Box.of(-1, 0),
                                                    Interval32Box.of(55, 81915457)))
                         );
    }
}
