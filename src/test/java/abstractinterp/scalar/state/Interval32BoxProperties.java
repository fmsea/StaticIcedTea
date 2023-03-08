package abstractinterp.scalar.state;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.stream.Stream;
import net.jqwik.api.Example;
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
        Interval32Box box = Interval32Box.of(lower, upper);
        assertTrue(box.isBounded());
    }

    @Property
    void unboundedBoxesAreUnbounded(@ForAll @Negative int x,
                                    @ForAll @Positive int y) {
        Interval32Box box = Interval32Box.of(y, x);
        assertAll(() -> assertFalse(box.isBounded()),
                  () -> assertTrue(box.isBottom()));
    }

    @Property
    void intervalsContainPoints(@ForAll int lower,
                                @ForAll int upper) {
        Interval32Box box = Interval32Box.of(lower, upper);
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
        if (xl > yl) {
            assertFalse(z.isLowerBounded());
        } else {
            assertAll(() -> assertTrue(z.isLowerBounded()),
                      () -> assertEquals(xl, z.lowerBoundOrElse()));
        }
        if (xu < yu) {
            assertFalse(z.isUpperBounded());
        } else {
            assertAll(() -> assertTrue(z.isUpperBounded()),
                      () -> assertEquals(xu, z.upperBoundOrElse()));
        }
    }

    @Property
    void widenWithBottomTakesOther(@ForAll Interval32Box box) {
        Interval32Box a = Interval32Box.of(box);
        Interval32Box bot = Interval32Box.BOT();
        a.wideningAssign(bot, Optional.empty());
        bot.wideningAssign(box, Optional.empty());
        assertAll(() -> assertFalse(a.isBottom()),
                  () -> assertFalse(bot.isBottom()));
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
        Interval32Box box = Interval32Box.of(x, y);
        box.negate();
        assertAll(() -> assertEquals(y * -1, box.lowerBound().get()),
                  () -> assertEquals(x * -1, box.upperBound().get()));
    }

    @Property
    boolean intervalsCanBeSingletons(@ForAll int x) {
        Interval32Box box = Interval32Box.of(x, x);
        return box.isSingleton();
    }

    @Property
    void equalIntervalsAreEqual(@ForAll int x, @ForAll int y) {
        {
            Interval32Box xBox = Interval32Box.of(null, x);
            Interval32Box yBox = Interval32Box.of(null, x);
            assertAll(() -> assertTrue(xBox.equals(yBox)),
                      () -> assertTrue(yBox.equals(xBox)));
        }
        {
            Interval32Box xBox = Interval32Box.of(x, null);
            Interval32Box yBox = Interval32Box.of(x, null);
            assertAll(() -> assertTrue(xBox.equals(yBox)),
                      () -> assertTrue(yBox.equals(xBox)));
        }

        {
            Interval32Box xBox = Interval32Box.of(x, y);
            Interval32Box yBox = Interval32Box.of(x, y);
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
    boolean testTransferConditionEq(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Eq);
        if ((x.lowerBound().flatMap(xl -> y.upperBound().map(yu -> xl > yu)).orElse(false)) ||
            (y.lowerBound().flatMap(yl -> x.upperBound().map(xu -> yl > xu)).orElse(false))) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionNe(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ne);
        if (x.isSingleton() && y.isSingleton() && x.equals(y)) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionLt(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        if (x.lowerBound().flatMap(a -> y.upperBound().map(d -> a >= d)).orElse(false)) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionLTChanges(@ForAll Interval32Box x,
                                           @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Lt);
        if (x.upperBound().flatMap(b -> y.lowerBound().map(c -> b < c)).orElse(false)) {
            return result.get(0).equals(x) && result.get(1).equals(y);
        } else {
            return true;
        }
    }

    @Property
    boolean testTransferConditionLe(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
        if (x.lowerBound().flatMap(a -> y.upperBound().map(d -> a > d)).orElse(false)) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionLTEChanges(@ForAll Interval32Box x,
                                            @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Le);
        if (x.upperBound().flatMap(b -> y.lowerBound().map(c -> b <= c)).orElse(false)) {
            return result.get(0).equals(x) && result.get(1).equals(y);
        } else {
            return true;
        }
    }

    @Property
    boolean testTransferConditionGt(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        if (y.lowerBound().flatMap(c -> x.upperBound().map(b -> c >= b)).orElse(false)) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionGTChanges(@ForAll Interval32Box x,
                                           @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Gt);
        if (x.lowerBound().flatMap(a -> y.upperBound().map(d -> a > d)).orElse(false)) {
            return result.get(0).equals(x) && result.get(1).equals(y);
        } else {
            return true;
        }
    }

    @Property
    boolean testTransferConditionGe(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        if (y.lowerBound().flatMap(c -> x.upperBound().map(b -> c > b)).orElse(false)) {
            return result.stream().map(r -> r.isBottom()).reduce((r1, r2) -> r1 && r2).get();
        } else {
            return result.stream().map(r -> r.isValid()).reduce((r1, r2) -> r1 && r2).get();
        }
    }

    @Property
    boolean testTransferConditionGTEChanges(@ForAll Interval32Box x,
                                            @ForAll Interval32Box y) {
        List<Interval32Box> result = Interval32Box.transferCondition(x, y, PredicateType.Ge);
        if (y.upperBound().flatMap(d -> x.lowerBound().map(a -> d <= a)).orElse(false)) {
            return result.get(0).equals(x) && result.get(1).equals(y);
        } else {
            return true;
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

    @Property
    void intervalEquality(@ForAll Interval32Box box) {
        assertTrue(box.equals(box));
    }

    @Property
    void translationMappingDoesNotAlterOriginal(@ForAll Interval32Box box) {
        Interval32Box orig = Interval32Box.of(box);
        box.lowerBound().map(b -> b * -1);
        box.upperBound().map(b -> b * -1);
        assertAll(() -> assertEquals(orig, box));
    }

    @Property
    void unionIsSymmetric(@ForAll Interval32Box a, @ForAll Interval32Box b) {
        assertEquals(Interval32Box.union(a, b),
                     Interval32Box.union(b, a));
    }

    @Property
    void unionIsAssoc(@ForAll List<Interval32Box> intervals) {
        assertEquals(intervals.stream().reduce(Interval32Box::union),
                     intervals.stream().sorted().reduce(Interval32Box::union));
    }

    @Property
    void unionWithBOTisOther(@ForAll Interval32Box a) {
        assertAll(() -> assertEquals(a, Interval32Box.union(a, Interval32Box.BOT())),
                  () -> assertEquals(a, Interval32Box.union(Interval32Box.BOT(), a)));
    }

    @Property
    void unionWithTOPIsTOP(@ForAll Interval32Box a) {
        assertAll(() -> assertEquals(Interval32Box.TOP(), Interval32Box.union(a, Interval32Box.TOP())),
                  () -> assertEquals(Interval32Box.TOP(), Interval32Box.union(Interval32Box.TOP(), a)));
    }

    @Property
    void intervalsUnionedWithTOPisTOP(@ForAll List<Interval32Box> intervals) {
        assertEquals(Interval32Box.TOP(),
                     Stream.concat(intervals.stream(),
                                   Stream.of(Interval32Box.TOP()))
                     .reduce(Interval32Box::union).get());
    }

    @Example
    void integerCoverageUnionsToTOP() {
        assertEquals(Interval32Box.TOP(),
                     Stream.of(Interval32Box.of(null, -1),
                               Interval32Box.of(0),
                               Interval32Box.of(1, null))
                     .reduce(Interval32Box::union).get());
    }

    @Property
    void intersectionOfIntervalsIsSymmetric(@ForAll Interval32Box a, @ForAll Interval32Box b) {
        assertEquals(a.intersects(b), b.intersects(a));
    }

    @Property
    void interleavingIntervalsIsSymmetric(@ForAll Interval32Box a, @ForAll Interval32Box b) {
        Set<Interval32Box> forwards = Interval32Box.interleave(a, b);
        Set<Interval32Box> backwards = Interval32Box.interleave(b, a);
        assertEquals(forwards, backwards);
    }

    @Property
    void interwovenIntervalsAreDisjoint(@ForAll Interval32Box a, @ForAll Interval32Box b) {
        Set<Interval32Box> woven = Interval32Box.interleave(a, b);
        woven.stream().forEach(x -> {
                woven.stream().forEach(y -> {
                        if (!x.equals(y)) {
                            assertFalse(x.intersects(y));
                            assertFalse(y.intersects(x));
                        }
                    });
            });
    }

    @Property
    void intervalsAreSubsetsOfUnion(@ForAll Interval32Box a, @ForAll Interval32Box b) {
        Interval32Box union = Interval32Box.union(a, b);
        assertAll(() -> assertTrue(a.isSubset(union)),
                  () -> assertTrue(b.isSubset(union)));
    }
}
