package abstractinterp.scalar.state;

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
        Assertions.assertEquals(String.format("%s >= %d & %s <= %d",
                                              l.toString(),
                                              b.lowerBound(),
                                              l.toString(),
                                              b.upperBound()),
                                b.toGrimpExpr(l).toString());
    }
}
