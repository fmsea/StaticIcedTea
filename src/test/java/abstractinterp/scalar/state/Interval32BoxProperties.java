package abstractinterp.scalar.state;

import java.util.List;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Negative;
import net.jqwik.api.constraints.Positive;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import soot.Local;

public class Interval32BoxProperties {

    @Property
    void boundedBoxesAreBounded(@ForAll int x,
                                @ForAll int y) {
        int lower = Math.min(x, y);
        int upper = Math.max(x, y);
        Interval32Box box = new Interval32Box(lower, upper);
        assertTrue(box.isBounded());
    }

    @Property
    void unboundedBoxesAreUnbounded(@ForAll @Negative int x,
                                    @ForAll @Positive int y) {
        Interval32Box box = new Interval32Box(y, x);
        assertAll(() -> assertFalse(box.isBounded()),
                  () -> assertTrue(box.isBottom()));
    }

    @Property
    void intervalsContainPoints(@ForAll int lower,
                                @ForAll int upper) {
        Interval32Box box = new Interval32Box(lower, upper);
        if (lower <= upper) {
            assertTrue(box.containsIntegerPoint());
        } else {
            assertAll(() -> assertTrue(box.isBottom()),
                      () -> assertFalse(box.containsIntegerPoint()));
        }
    }

    @Property
    void upperBoundAssignment(@ForAll Interval32Box x,
                              @ForAll Interval32Box y) {
        int lower = Math.min(x.lowerBoundOrElse(), y.lowerBoundOrElse());
        int upper = Math.max(x.upperBoundOrElse(), y.upperBoundOrElse());
        x.upperBoundAssign(y);
        assertAll(() -> assertEquals(lower, x.lowerBound().get()),
                  () -> assertEquals(upper, x.upperBound().get()));
    }

    @Property
    void upperBoundAssignWithBottom(@ForAll Interval32Box c) {
        Interval32Box bot = Interval32Box.BOT();
        assertAll(() -> assertFalse((Interval32Box.upperBoundAssign(bot, c)).isBottom()),
                  () -> assertFalse((Interval32Box.upperBoundAssign(c, bot)).isBottom()));
    }

    @Property
    void widenAssign(@ForAll Interval32Box x,
                     @ForAll Interval32Box y) {
        int xl = x.lowerBoundOrElse();
        int xu = x.upperBoundOrElse();
        int yl = y.lowerBoundOrElse();
        int yu = y.upperBoundOrElse();
        Interval32Box z = Interval32Box.wideningAssign(x, y);
        if (xl < yl) {
            assertFalse(z.isLowerBounded());
        } else {
            assertAll(() -> assertTrue(z.isLowerBounded()),
                      () -> assertEquals(xl, z.lowerBoundOrElse()));
        }
        if (xu > yu) {
            assertFalse(z.isUpperBounded());
        } else {
            assertAll(() -> assertTrue(z.isUpperBounded()),
                      () -> assertEquals(xu, z.upperBoundOrElse()));
        }
    }

    @Property
    void widenWithBottomStaysBottom(@ForAll Interval32Box box) {
        Interval32Box a = new Interval32Box(box);
        Interval32Box bot = Interval32Box.BOT();
        a.wideningAssign(bot);
        bot.wideningAssign(box);
        assertAll(() -> assertFalse(a.isBottom()),
                  () -> assertTrue(bot.isBottom()));
    }

    @Property
    void everythingSubsetsTop(@ForAll Interval32Box box) {
        Interval32Box top = Interval32Box.TOP();
        assertTrue(box.isSubset(top));
    }

    @Property
    void bottomIsAlwaysSubset(@ForAll Interval32Box box) {
        Interval32Box bot = Interval32Box.BOT();
        assertTrue(bot.isSubset(box));
    }

    @Property
    void noIntervalSubsetsBottom(@ForAll Interval32Box box) {
        Interval32Box bot = Interval32Box.BOT();
        assertFalse(box.isSubset(bot));
    }

    @Property
    void negateInterval(@ForAll int x, @ForAll int y) {
        Interval32Box box = new Interval32Box(x, y);
        box.negate();
        assertAll(() -> assertEquals(y * -1, box.lowerBound().get()),
                  () -> assertEquals(x * -1, box.upperBound().get()));
    }

    @Property
    void bottomIntersectsWithNothing(@ForAll Interval32Box box) {
        Interval32Box bot = Interval32Box.BOT();
        assertAll(() -> assertFalse(bot.intersects(box)),
                  () -> assertFalse(box.intersects(bot)));
    }

    @Property
    void topAlwaysIntersects(@ForAll Interval32Box box) {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        assertAll(() -> assertTrue(top.intersects(box)),
                  () -> assertTrue(max.intersects(box)),
                  () -> assertTrue(box.intersects(top)),
                  () -> assertTrue(box.intersects(max)));
    }

    @Property
    boolean selfIntersectsWithSelf(@ForAll Interval32Box box) {
        return box.intersects(box);
    }

    @Property
    boolean intervalsCanBeSingletons(@ForAll int x) {
        Interval32Box box = new Interval32Box(x, x);
        return box.isSingleton();
    }

    @Property
    void equalIntervalsAreEqual(@ForAll int x, @ForAll int y) {
        {
            Interval32Box xBox = new Interval32Box(null, x);
            Interval32Box yBox = new Interval32Box(null, x);
            assertAll(() -> assertTrue(xBox.equals(yBox)),
                      () -> assertTrue(yBox.equals(xBox)));
        }
        {
            Interval32Box xBox = new Interval32Box(x, null);
            Interval32Box yBox = new Interval32Box(x, null);
            assertAll(() -> assertTrue(xBox.equals(yBox)),
                      () -> assertTrue(yBox.equals(xBox)));
        }

        {
            Interval32Box xBox = new Interval32Box(x, y);
            Interval32Box yBox = new Interval32Box(x, y);
            assertAll(() -> assertTrue(xBox.equals(yBox)),
                      () -> assertTrue(yBox.equals(xBox)));
        }
    }

    @Property
    void toGrimpExprStringsAreCorrect(@ForAll Interval32Box b, @ForAll Local l) {
        if (b.isSingleton()) {
            assertEquals(String.format("%s == %d",
                                       l.toString(),
                                       b.lowerBoundOrElse()),
                         b.toGrimpExpr(l).toString());
        } else {
            assertEquals(String.format("%s >= %d & %s <= %d",
                                       l.toString(),
                                       b.lowerBoundOrElse(),
                                       l.toString(),
                                       b.upperBoundOrElse()),
                         b.toGrimpExpr(l).toString());
        }
    }

    @Property
    void transferCondReturns2Intervals(@ForAll Interval32Box x,
                                       @ForAll Interval32Box y,
                                       @ForAll PredicateType t) {
        List<Interval32Box> z = Interval32Box.transferCondition(x, y, t);
        assertEquals(2, z.size());
    }

    @Property
    void testTransferConditionEq(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        assertEquals(2, result.size());
        if (position == (byte) 0 || position == (byte) 4) {
            for (Interval32Box b : result) {
                assertTrue(b.isBottom());
            }
        } else if (position == (byte) 1) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(y.lowerBound(), x.upperBound());
                assertEquals(expected, b);
            }
        } else if (position == (byte) 2) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(y);
                assertEquals(expected, b);
            }
        } else if (position == (byte) 3) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(x.lowerBound(), y.upperBound());
                assertEquals(expected, b);
            }
        } else if (position == (byte) 5) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(x);
                assertEquals(expected, b);
            }
        }
    }

    @Property
    void testTransferConditionNe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        assertEquals(2, result.size());
        if (x.equals(y)) {
            assertAll(result.stream().map(b -> () -> assertTrue(b.isBottom())));
        } else {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        }
    }

    @Property
    void testTransferConditionLe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Le);
        assertEquals(2, result.size());
        if (position == (byte) 4) {
            assertAll(result.stream().map(b -> () -> assertTrue(b.isBottom())));
        } else if (position == (byte) 0 || position == (byte) 1) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 2) {
            Interval32Box x_expected = new Interval32Box(x.lowerBound(), y.upperBound());
            assertAll(() -> assertEquals(x_expected, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 3) {
            Interval32Box expected = new Interval32Box(x.lowerBound(), y.upperBound());
            assertAll(result.stream().map(b -> () -> assertEquals(expected, b)));
        } else if (position == (byte) 5) {
            Interval32Box y_expected = new Interval32Box(x.lowerBound(), y.upperBound());
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y_expected, result.get(1)));
        }
    }

    @Property
    void testTransferConditionLt(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        assertEquals(2, result.size());
        if (x.equals(y) || position == (byte) 4) {
            assertAll(result.stream().map(b -> () -> assertTrue(b.isBottom())));
        } else if (position == (byte) 0 || position == (byte) 1) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 2) {
            Interval32Box x_expected = new Interval32Box(x.lowerBound().map(l -> Integer.valueOf(l)),
                                                         y.upperBound().map(u -> Integer.valueOf(u - 1)));
            assertEquals(x_expected, result.get(0));
            if (x.lowerBound().equals(y.lowerBound())) {
                assertEquals(new Interval32Box(y.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                               y.upperBound().map(u -> Integer.valueOf(u))),
                             result.get(1));
            } else {
                assertEquals(y, result.get(1));
            }
        } else if (position == (byte) 3) {
            assertAll(() -> assertEquals(new Interval32Box(x.lowerBound().map(l -> Integer.valueOf(l)),
                                                           y.upperBound().map(u -> Integer.valueOf(u - 1))),
                                         result.get(0)),
                      () -> assertEquals(new Interval32Box(x.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                                           y.upperBound().map(u -> Integer.valueOf(u))),
                                         result.get(1)));
        } else if (position == (byte) 5) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(new Interval32Box(x.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                                           y.upperBound().map(u -> Integer.valueOf(u))),
                                         result.get(1)));
        }
    }

    @Property
    void testTransferConditionGe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        assertEquals(2, result.size());
        if (position == (byte) 0) {
            assertAll(result.stream().map(b -> () -> assertTrue(b.isBottom())));
        } else if (position == (byte) 3 || position == (byte) 4) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 1) {
            assertAll(result.stream().map(b -> () -> assertEquals(new Interval32Box(y.lowerBound(),
                                                                                    x.upperBound()),
                                                                  b)));
        } else if (position == (byte) 2) {
            assertAll(() -> assertEquals(new Interval32Box(y.lowerBound(), x.upperBound()),
                                         result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 5) {
            assertAll(result.stream().map(b -> () -> assertEquals(new Interval32Box(x.lowerBound(),
                                                                                    x.upperBound()),
                                                                  b)));
        }
    }

    @Property
    void testTransferConditionGt(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        assertEquals(2, result.size());
        if (x.equals(y) || position == (byte) 0) {
            assertAll(result.stream().map(b -> () -> assertTrue(b.isBottom())));
        } else if (position == (byte) 3 || position == (byte) 4) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 1) {
            assertAll(() -> assertEquals(new Interval32Box(y.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                                           x.upperBound().map(u -> Integer.valueOf(u))),
                                         result.get(0)),
                      () -> assertEquals(new Interval32Box(y.lowerBound().map(l -> Integer.valueOf(l)),
                                                           x.upperBound().map(u -> Integer.valueOf(u - 1))),
                                         result.get(1)));
        } else if (position == (byte) 2) {
            assertAll(() -> assertEquals(new Interval32Box(y.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                                           x.upperBound().map(u -> Integer.valueOf(u))),
                                         result.get(0)),
                      () -> assertEquals(y, result.get(1)));
        } else if (position == (byte) 5) {
            assertAll(() -> assertEquals(x, result.get(0)),
                      () -> assertEquals(new Interval32Box(x.lowerBound().map(l -> Integer.valueOf(l + 1)),
                                                           x.upperBound().map(u -> Integer.valueOf(u - 1))),
                                         result.get(1)));
        }
    }

    @Property
    void testIntervalAdditionWithBottom(@ForAll Interval32Box c) {
        assertAll(() -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.add(c, Interval32Box.BOT())),
                  () -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.add(Interval32Box.BOT(), c)));
    }

    @Property
    void testIntervalSubtractionWithBottom(@ForAll Interval32Box c) {
        assertAll(() -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.subtract(c, Interval32Box.BOT())),
                  () -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.subtract(Interval32Box.BOT(), c)));
    }

    @Property
    void testIntervalMultiplicationWithBottom(@ForAll Interval32Box c) {
        assertAll(() -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.multiply(c, Interval32Box.BOT())),
                  () -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.multiply(Interval32Box.BOT(), c)));
    }

    @Property
    void testIntervalDivisionWithBottom(@ForAll Interval32Box c) {
        assertAll(() -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.divide(c, Interval32Box.BOT())),
                  () -> assertEquals(Interval32Box.BOT(),
                                     Interval32Box.divide(Interval32Box.BOT(), c)));
    }
}
