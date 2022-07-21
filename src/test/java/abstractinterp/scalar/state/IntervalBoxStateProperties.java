package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.lifecycle.BeforeProperty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import soot.Local;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class IntervalBoxStateProperties {

    private SolverWrapper solver;

    @BeforeProperty
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Property
    void negateReturnsNewBox(@ForAll Interval32Box box) {
        Interval32Box negatedBox = IntervalBoxState.negate(box);
        assertAll(// memory location test
                  () -> assertTrue(box != negatedBox),
                  // equality test
                  () -> assertEquals(box.lowerBound(), negatedBox.lowerBound()),
                  () -> assertEquals(box.upperBound(), negatedBox.upperBound()));
    }

    @Property
    void transferBinaryAddition(@ForAll Interval32Box x,
                                @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.ADDITION);
        if (x.lowerBound().isEmpty() || y.lowerBound().isEmpty()) {
            assertEquals(Optional.empty(), z.lowerBound());
        } else {
            try {
                assertEquals(Math.addExact(x.lowerBound().get(), y.lowerBound().get()),
                             z.lowerBound().get());
            } catch (ArithmeticException ex) {
                assertTrue(z.lowerBound().isEmpty());
            }
        }

        if (x.upperBound().isEmpty() || y.upperBound().isEmpty()) {
            assertEquals(Optional.empty(), z.upperBound());
        } else {
            try {
                assertEquals(Math.addExact(x.upperBound().get(), y.upperBound().get()),
                             z.upperBound().get());
            } catch (ArithmeticException ex) {
                assertTrue(z.upperBound().isEmpty());
            }
        }
    }

    @Property
    void transferBinarySubtraction(@ForAll Interval32Box x,
                                   @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.SUBTRACTION);
        if (x.lowerBound().isEmpty() || y.upperBound().isEmpty()) {
            assertEquals(Optional.empty(), z.lowerBound());
        } else {
            try {
                assertEquals(Math.subtractExact(x.lowerBound().get(), y.upperBound().get()),
                             z.lowerBound().get());
            } catch (ArithmeticException ex) {
                assertTrue(z.lowerBound().isEmpty());
            }
        }

        if (x.upperBound().isEmpty() || y.lowerBound().isEmpty()) {
            assertEquals(Optional.empty(), z.upperBound());
        } else {
            try {
                assertEquals(Math.subtractExact(x.upperBound().get(),
                                                y.lowerBound().get()),
                             z.upperBound().get());
            } catch (ArithmeticException ex) {
                assertTrue(z.upperBound().isEmpty());
            }
        }
    }

    @Property
    void transferBinaryMultiplcation(@ForAll Interval32Box x,
                                     @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.MULTIPLICATION);
        try {
            Optional<Integer> low = Optional.of(minimum(Math.multiplyExact(x.lowerBoundOrElse(),
                                                                           y.lowerBoundOrElse()),
                                                        Math.multiplyExact(x.lowerBoundOrElse(),
                                                                           y.upperBoundOrElse()),
                                                        Math.multiplyExact(x.upperBoundOrElse(),
                                                                           y.lowerBoundOrElse()),
                                                        Math.multiplyExact(x.upperBoundOrElse(),
                                                                           y.upperBoundOrElse())));
            Optional<Integer> high = Optional.of(maximum(Math.multiplyExact(x.lowerBoundOrElse(),
                                                                            y.lowerBoundOrElse()),
                                                         Math.multiplyExact(x.lowerBoundOrElse(),
                                                                            y.upperBoundOrElse()),
                                                         Math.multiplyExact(x.upperBoundOrElse(),
                                                                            y.lowerBoundOrElse()),
                                                         Math.multiplyExact(x.upperBoundOrElse(),
                                                                            y.upperBoundOrElse())));
            assertAll(() -> assertEquals(low, z.lowerBound()),
                      () -> assertEquals(high, z.upperBound()));
        } catch (ArithmeticException ex) {
            assertAll(() -> assertTrue(z.lowerBound().isEmpty()),
                      () -> assertTrue(z.upperBound().isEmpty()),
                      () -> assertTrue(z.isTop()));
        }
    }

    @Property
    void transferBinaryDivision(@ForAll Interval32Box x,
                                @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.DIVISION);
        BiFunction<Integer, Integer, Integer> div = (a, b) -> {
            if (a == Integer.MIN_VALUE && b == -1) {
                return Integer.MAX_VALUE;
            } else {
                return a / b;
            }
        };
        if (y.lowerBoundOrElse() > 0 || y.upperBoundOrElse() < 0) {
            Integer low = minimum(div.apply(x.lowerBoundOrElse(), y.lowerBoundOrElse()),
                                  div.apply(x.lowerBoundOrElse(), y.upperBoundOrElse()),
                                  div.apply(x.upperBoundOrElse(), y.lowerBoundOrElse()),
                                  div.apply(x.upperBoundOrElse(), y.upperBoundOrElse()));
            Integer high = maximum(div.apply(x.lowerBoundOrElse(), y.lowerBoundOrElse()),
                                   div.apply(x.lowerBoundOrElse(), y.upperBoundOrElse()),
                                   div.apply(x.upperBoundOrElse(), y.lowerBoundOrElse()),
                                   div.apply(x.upperBoundOrElse(), y.upperBoundOrElse()));
            assertAll(() -> assertEquals(low, z.lowerBoundOrElse()),
                      () -> assertEquals(high, z.upperBoundOrElse()));
        } else if (y.lowerBoundOrElse() == 0 && y.upperBoundOrElse() != 0) {
            Integer low = minimum(div.apply(x.lowerBoundOrElse(), y.upperBoundOrElse()),
                                  div.apply(x.upperBoundOrElse(), y.upperBoundOrElse()));
            assertAll(() -> assertEquals(low, z.lowerBoundOrElse()),
                      () -> assertEquals(Integer.MAX_VALUE, z.upperBoundOrElse()));
        } else if (y.upperBoundOrElse() == 0 && y.lowerBoundOrElse() != 0) {
            Integer high = maximum(div.apply(x.lowerBoundOrElse(), y.lowerBoundOrElse()),
                                   div.apply(x.upperBoundOrElse(), y.lowerBoundOrElse()));
            assertAll(() -> assertEquals(Integer.MIN_VALUE, z.lowerBoundOrElse()),
                      () -> assertEquals(high, z.upperBoundOrElse()));
        } else {
            assertAll(() -> assertTrue(z.lowerBound().isEmpty()),
                      () -> assertTrue(z.upperBound().isEmpty()),
                      () -> assertTrue(z.isTop()));
        }
    }

    @Property
    void getConnectedPropertiesOfAlwaysReturnsItself(@ForAll Set<Local> locals) {
        IntervalBoxState state = new IntervalBoxState(locals, true);
        assertAll(locals.stream().map(l -> () -> assertEquals(Set.of(l), state.getConnectedVariablesOf(l))));
    }

    private int minimum(int... xs) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < xs.length; i++) {
            min = Math.min(min, xs[i]);
        }
        return min;
    }

    private int maximum(int... xs) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < xs.length; i++) {
            max = Math.max(max, xs[i]);
        }
        return max;
    }
}
