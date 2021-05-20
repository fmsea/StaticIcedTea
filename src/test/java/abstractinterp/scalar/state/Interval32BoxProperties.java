package abstractinterp.scalar.state;

import java.util.List;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Negative;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.Assertions;
import soot.Local;

public class Interval32BoxProperties {

    @Property
    void boundedBoxesAreBounded(@ForAll int x,
                                @ForAll int y) {
        int lower = Math.min(x, y);
        int upper = Math.max(x, y);
        Interval32Box box = new Interval32Box(lower, upper);
        Assertions.assertTrue(box.isBounded());
    }

    @Property
    void unboundedBoxesAreUnbounded(@ForAll @Negative int x,
                                    @ForAll @Positive int y) {
        Interval32Box box = new Interval32Box(y, x);
        Assertions.assertFalse(box.isBounded());
        Assertions.assertTrue(box.isBottom());
    }

    @Property
    void intervalsContainPoints(@ForAll int lower,
                                @ForAll int upper) {
        Interval32Box box = new Interval32Box(lower, upper);
        if (lower <= upper) {
            Assertions.assertTrue(box.containsIntegerPoint());
        } else {
            Assertions.assertTrue(box.isBottom());
            Assertions.assertFalse(box.containsIntegerPoint());
        }
    }

    @Property
    void upperBoundAssignment(@ForAll Interval32Box x,
                              @ForAll Interval32Box y) {
        int lower = Math.min(x.lowerBound(), y.lowerBound());
        int upper = Math.max(x.upperBound(), y.upperBound());
        x.upperBoundAssign(y);
        Assertions.assertEquals(lower, x.lowerBound());
        Assertions.assertEquals(upper, x.upperBound());
    }

    @Property
    void widenAssign(@ForAll Interval32Box x,
                     @ForAll Interval32Box y) {
        int x1 = x.lowerBound();
        int y1 = x.upperBound();
        int x2 = y.lowerBound();
        int y2 = y.upperBound();
        x.wideningAssign(y);
        if (x2 < x1) {
            Assertions.assertEquals(Integer.MIN_VALUE, x.lowerBound());
        } else {
            Assertions.assertEquals(x1, x.lowerBound());
        }
        if (y1 < y2) {
            Assertions.assertEquals(Integer.MAX_VALUE, x.upperBound());
        } else {
            Assertions.assertEquals(y1, x.upperBound());
        }
    }

    @Property
    void negateInterval(@ForAll int x, @ForAll int y) {
        Interval32Box box = new Interval32Box(x, y);
        box.negate();
        Assertions.assertEquals(y * -1, box.lowerBound());
        Assertions.assertEquals(x * -1, box.upperBound());
    }

    @Property
    void bottomIntersectsWithNothing(@ForAll Interval32Box box) {
        Interval32Box bot = Interval32Box.BOT();
        Assertions.assertFalse(bot.intersects(box));
        Assertions.assertFalse(box.intersects(bot));
    }

    @Property
    void topAlwaysIntersects(@ForAll Interval32Box box) {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(top.intersects(box));
        Assertions.assertTrue(max.intersects(box));
        Assertions.assertTrue(box.intersects(top));
        Assertions.assertTrue(box.intersects(max));
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
        Interval32Box xBox = new Interval32Box(null, x);
        Interval32Box yBox = new Interval32Box(null, x);
        Assertions.assertTrue(xBox.equals(yBox));
        Assertions.assertTrue(yBox.equals(xBox));
        xBox = new Interval32Box(x, null);
        yBox = new Interval32Box(x, null);
        Assertions.assertTrue(xBox.equals(yBox));
        Assertions.assertTrue(yBox.equals(xBox));
        xBox = new Interval32Box(x, y);
        yBox = new Interval32Box(x, y);
        Assertions.assertTrue(xBox.equals(yBox));
        Assertions.assertTrue(yBox.equals(xBox));
    }

    @Property
    void toGrimpExprStringsAreCorrect(@ForAll Interval32Box b, @ForAll Local l) {
        if (b.isSingleton()) {
            Assertions.assertEquals(String.format("%s == %d",
                                                  l.toString(),
                                                  b.lowerBound()),
                                    b.toGrimpExpr(l).toString());
        } else {
            Assertions.assertEquals(String.format("%s >= %d & %s <= %d",
                                                  l.toString(),
                                                  b.lowerBound(),
                                                  l.toString(),
                                                  b.upperBound()),
                                    b.toGrimpExpr(l).toString());
    }
    }

    @Property
    void transferCondReturns2Intervals(@ForAll Interval32Box x,
                                       @ForAll Interval32Box y,
                                       @ForAll PredicateType t) {
        List<Interval32Box> z = Interval32Box.transferCondition(x, y, t);
        Assertions.assertEquals(2, z.size());
    }

    @Property
    void testTransferConditionEq(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        Assertions.assertEquals(2, result.size());
        if (position == (byte) 0 || position == (byte) 4) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else if (position == (byte) 1) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(y.lowerBound(), x.upperBound());
                Assertions.assertEquals(expected, b);
            }
        } else if (position == (byte) 2) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(y);
                Assertions.assertEquals(expected, b);
            }
        } else if (position == (byte) 3) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(x.lowerBound(), y.upperBound());
                Assertions.assertEquals(expected, b);
            }
        } else if (position == (byte) 5) {
            for (Interval32Box b : result) {
                Interval32Box expected = new Interval32Box(x);
                Assertions.assertEquals(expected, b);
            }
        }
    }

    @Property
    void testTransferConditionNe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        Assertions.assertEquals(2, result.size());
        if (x.equals(y)) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        }
    }

    @Property
    void testTransferConditionLe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Le);
        Assertions.assertEquals(2, result.size());
        if (position == (byte) 4) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else if (position == (byte) 0 || position == (byte) 1) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 2) {
            Interval32Box x_expected = new Interval32Box(x.lowerBound(), y.upperBound());
            Assertions.assertEquals(x_expected, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 3) {
            Interval32Box expected = new Interval32Box(x.lowerBound(), y.upperBound());
            for (Interval32Box b : result) {
                Assertions.assertEquals(expected, b);
            }
        } else if (position == (byte) 5) {
            Interval32Box y_expected = new Interval32Box(x.lowerBound(), y.upperBound());
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y_expected, result.get(1));
        }
    }

    @Property
    void testTransferConditionLt(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        Assertions.assertEquals(2, result.size());
        if (x.equals(y) || position == (byte) 4) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else if (position == (byte) 0 || position == (byte) 1) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 2) {
            Interval32Box x_expected = new Interval32Box(Integer.valueOf(x.lowerBound()),
                                                         Integer.valueOf(y.upperBound() - 1));
            Assertions.assertEquals(x_expected, result.get(0));
            if (x.lowerBound().equals(y.lowerBound())) {
                Assertions.assertEquals(new Interval32Box(Integer.valueOf(y.lowerBound() + 1),
                                                          Integer.valueOf(y.upperBound())),
                                        result.get(1));
            } else {
                Assertions.assertEquals(y, result.get(1));
            }
        } else if (position == (byte) 3) {
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(x.lowerBound()),
                                                      Integer.valueOf(y.upperBound() - 1)),
                                    result.get(0));
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(x.lowerBound() + 1),
                                                      Integer.valueOf(y.upperBound())),
                                    result.get(1));
        } else if (position == (byte) 5) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(x.lowerBound() + 1),
                                                      Integer.valueOf(y.upperBound())),
                                    result.get(1));
        }
    }

    @Property
    void testTransferConditionGe(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        Assertions.assertEquals(2, result.size());
        if (position == (byte) 0) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else if (position == (byte) 3 || position == (byte) 4) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 1) {
            for (Interval32Box b : result) {
                Assertions.assertEquals(new Interval32Box(y.lowerBound(),
                                                          x.upperBound()),
                                        b);
            }
        } else if (position == (byte) 2) {
            Assertions.assertEquals(new Interval32Box(y.lowerBound(), x.upperBound()),
                                    result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 5) {
            for (Interval32Box b : result) {
                Assertions.assertEquals(new Interval32Box(x.lowerBound(),
                                                          x.upperBound()),
                                        b);
            }
        }
    }

    @Property
    void testTransferConditionGt(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        List<Interval32Box> result;
        byte position = x.intersectionPosition(y);
        result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        Assertions.assertEquals(2, result.size());
        if (x.equals(y) || position == (byte) 0) {
            for (Interval32Box b : result) {
                Assertions.assertTrue(b.isBottom());
            }
        } else if (position == (byte) 3 || position == (byte) 4) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 1) {
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(y.lowerBound() + 1),
                                                      Integer.valueOf(x.upperBound())),
                                    result.get(0));
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(y.lowerBound()),
                                                      Integer.valueOf(x.upperBound() - 1)),
                                    result.get(1));
        } else if (position == (byte) 2) {
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(y.lowerBound() + 1),
                                                      Integer.valueOf(x.upperBound())),
                                    result.get(0));
            Assertions.assertEquals(y, result.get(1));
        } else if (position == (byte) 5) {
            Assertions.assertEquals(x, result.get(0));
            Assertions.assertEquals(new Interval32Box(Integer.valueOf(x.lowerBound() + 1),
                                                      Integer.valueOf(x.upperBound() - 1)),
                                    result.get(1));
        }
    }
}
