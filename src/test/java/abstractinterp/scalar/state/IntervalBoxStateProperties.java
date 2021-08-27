package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
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
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.ADDITION);
        if (x.lowerBound().isEmpty() || y.lowerBound().isEmpty()) {
            assertEquals(Optional.empty(), z.lowerBound());
        } else if (Optional.of(Integer.MIN_VALUE).equals(x.lowerBound()) ||
            Optional.of(Integer.MIN_VALUE).equals(y.lowerBound())) {
            assertEquals(Integer.MIN_VALUE, z.lowerBoundOrElse());
        } else {
            assertEquals(x.lowerBound().get() + y.lowerBound().get(),
                         z.lowerBound().get());
        }

        if (x.upperBound().isEmpty() || y.upperBound().isEmpty()) {
            assertEquals(Optional.empty(), z.upperBound());
        } else if (Optional.of(Integer.MAX_VALUE).equals(x.upperBound()) ||
                   Optional.of(Integer.MAX_VALUE).equals(y.upperBound())) {
            assertEquals(Integer.MAX_VALUE, z.upperBoundOrElse());
        } else {
            assertEquals(x.upperBound().get() + y.upperBound().get(),
                         z.upperBound().get());
        }
    }

    @Property
    void transferBinarySubtraction(@ForAll Interval32Box x,
                                   @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.SUBTRACTION);
        if (x.lowerBound().isEmpty() || y.upperBound().isEmpty()) {
            assertEquals(Optional.empty(), z.lowerBound());
        } else if (Optional.of(Integer.MIN_VALUE).equals(x.lowerBound()) ||
                   Optional.of(Integer.MAX_VALUE).equals(y.upperBound())) {
            assertEquals(Integer.MIN_VALUE, z.lowerBoundOrElse());
        } else {
            assertEquals(x.lowerBound().get() - y.upperBound().get(),
                         z.lowerBound().get());
        }

        if (x.upperBound().isEmpty() || y.lowerBound().isEmpty()) {
            assertEquals(Optional.empty(), z.upperBound());
        } else if (Optional.of(Integer.MAX_VALUE).equals(x.upperBound()) ||
                   Optional.of(Integer.MIN_VALUE).equals(y.lowerBound())) {
            assertEquals(Integer.MAX_VALUE, z.upperBoundOrElse());
        } else {
            assertEquals(x.upperBound().get() - y.lowerBound().get(),
                         z.upperBound().get());
        }
    }

    @Property
    void transferBinaryMultiplcation(@ForAll Interval32Box x,
                                     @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.MULTIPLICATION);
        int low = minimum(x.lowerBoundOrElse() * y.lowerBoundOrElse(),
                          x.lowerBoundOrElse() * y.upperBoundOrElse(),
                          x.upperBoundOrElse() * y.lowerBoundOrElse(),
                          x.upperBoundOrElse() * y.upperBoundOrElse());
        int high = maximum(x.lowerBoundOrElse() * y.lowerBoundOrElse(),
                           x.lowerBoundOrElse() * y.upperBoundOrElse(),
                           x.upperBoundOrElse() * y.lowerBoundOrElse(),
                           x.upperBoundOrElse() * y.upperBoundOrElse());
        assertAll(() -> assertEquals(low, z.lowerBoundOrElse()),
                  () -> assertEquals(high, z.upperBoundOrElse()));
    }

    @Property
    void transferBinaryDivision(@ForAll Interval32Box x,
                                @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.DIVISION);
        if (y.lowerBoundOrElse() > 0 || y.upperBoundOrElse() < 0) {
            Integer low = minimum(x.lowerBoundOrElse() / y.lowerBoundOrElse(),
                                  x.lowerBoundOrElse() / y.upperBoundOrElse(),
                                  x.upperBoundOrElse() / y.lowerBoundOrElse(),
                                  x.upperBoundOrElse() / y.upperBoundOrElse());
            Integer high = maximum(x.lowerBoundOrElse() / y.lowerBoundOrElse(),
                                   x.lowerBoundOrElse() / y.upperBoundOrElse(),
                                   x.upperBoundOrElse() / y.lowerBoundOrElse(),
                                   x.upperBoundOrElse() / y.upperBoundOrElse());
            assertAll(() -> assertEquals(low, z.lowerBoundOrElse()),
                      () -> assertEquals(high, z.upperBoundOrElse()));
        } else if (y.lowerBoundOrElse() == 0 && y.upperBoundOrElse() != 0) {
            Integer low = minimum(x.lowerBoundOrElse() / y.upperBoundOrElse(),
                                  x.upperBoundOrElse() / y.upperBoundOrElse());
            assertAll(() -> assertEquals(low, z.lowerBoundOrElse()),
                      () -> assertEquals(Integer.MAX_VALUE, z.upperBoundOrElse()));
        } else if (y.upperBoundOrElse() == 0 && y.lowerBoundOrElse() != 0) {
            Integer high = maximum(x.lowerBoundOrElse() / y.lowerBoundOrElse(),
                                   x.upperBoundOrElse() / y.lowerBoundOrElse());
            assertAll(() -> assertEquals(Integer.MIN_VALUE, z.lowerBoundOrElse()),
                      () -> assertEquals(high, z.upperBoundOrElse()));
        } else {
            assertAll(() -> assertEquals(Integer.MIN_VALUE, z.lowerBoundOrElse()),
                      () -> assertEquals(Integer.MAX_VALUE, z.upperBoundOrElse()));
        }
    }

    @Property
    void toSMT(@ForAll Map<Local, Interval32Box> states) {
        Set<Local> locals = states.keySet();
        IntervalBoxState state = new IntervalBoxState(locals, false);
        states.forEach((l, b) -> state.update(l, b));
        List<String> expected = states.entrySet().stream().map(e -> {
                Local l = e.getKey();
                Interval32Box b = e.getValue();
                return String.format("%s->%s", l, b.toSMT(l, this.solver));
            }).sorted().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<String> result = Arrays.stream(state.toSMT(this.solver).split("\n"))
            .filter(s -> !s.isEmpty())
            .sorted()
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
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
