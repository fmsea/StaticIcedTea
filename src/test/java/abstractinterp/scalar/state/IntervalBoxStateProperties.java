package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.statistics.StatisticsReport;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Assertions;
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
        // memory location test
        Assertions.assertTrue(box != negatedBox);
        // equality test
        Assertions.assertEquals(box.lowerBound(), negatedBox.lowerBound());
        Assertions.assertEquals(box.upperBound(), negatedBox.upperBound());
    }

    @Property
    void transferBinaryAddition(@ForAll Interval32Box x,
                                @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.ADDITION);
        if (x.lowerBound() == Integer.MIN_VALUE || y.lowerBound() == Integer.MIN_VALUE) {
            Assertions.assertEquals(Integer.MIN_VALUE, z.lowerBound());
        } else {
            Assertions.assertEquals(x.lowerBound() + y.lowerBound(), z.lowerBound());
        }
        if (x.upperBound() == Integer.MAX_VALUE || y.upperBound() == Integer.MAX_VALUE) {
            Assertions.assertEquals(Integer.MAX_VALUE, z.upperBound());
        } else {
            Assertions.assertEquals(x.upperBound() + y.upperBound(), z.upperBound());
        }
    }

    @Property
    void transferBinarySubtraction(@ForAll Interval32Box x,
                                   @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(x.lowerBound() - y.upperBound(), z.lowerBound());
        Assertions.assertEquals(x.upperBound() - y.lowerBound(), z.upperBound());
    }

    @Property
    void transferBinaryMultiplcation(@ForAll Interval32Box x,
                                     @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.MULTIPLICATION);
        int low = minimum(x.lowerBound() * y.lowerBound(),
                          x.lowerBound() * y.upperBound(),
                          x.upperBound() * y.lowerBound(),
                          x.upperBound() * y.upperBound());
        int high = maximum(x.lowerBound() * y.lowerBound(),
                           x.lowerBound() * y.upperBound(),
                           x.upperBound() * y.lowerBound(),
                           x.upperBound() * y.upperBound());
        Assertions.assertEquals(low, z.lowerBound());
        Assertions.assertEquals(high, z.upperBound());
    }

    @Property
    void transferBinaryDivision(@ForAll Interval32Box x,
                                @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperator.DIVISION);
        int low = Integer.MIN_VALUE;
        int high = Integer.MAX_VALUE;
        if (y.lowerBound() > 0 || y.upperBound() < 0) {
            low = minimum(x.lowerBound() / y.lowerBound(),
                          x.lowerBound() / y.upperBound(),
                          x.upperBound() / y.lowerBound(),
                          x.upperBound() / y.upperBound());
            high = maximum(x.lowerBound() / y.lowerBound(),
                           x.lowerBound() / y.upperBound(),
                           x.upperBound() / y.lowerBound(),
                           x.upperBound() / y.upperBound());
        } else if (y.lowerBound() == 0 && y.upperBound() != 0) {
            low = minimum(x.lowerBound() / y.upperBound(),
                          x.upperBound() / y.upperBound());
        } else if (y.upperBound() == 0 && y.lowerBound() != 0) {
            high = maximum(x.lowerBound() / y.lowerBound(),
                           x.upperBound() / y.lowerBound());
        }
        Assertions.assertEquals(low, z.lowerBound());
        Assertions.assertEquals(high, z.upperBound());
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
        Assertions.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertEquals(expected.get(i), result.get(i));
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
