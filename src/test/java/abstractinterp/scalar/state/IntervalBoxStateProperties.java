package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.statistics.StatisticsReport;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Assertions;
import soot.Local;

public class IntervalBoxStateProperties {

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
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 0);
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
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 1);
        Assertions.assertEquals(x.lowerBound() - y.upperBound(), z.lowerBound());
        Assertions.assertEquals(x.upperBound() - y.lowerBound(), z.upperBound());
    }

    @Property
    void transferBinaryMultiplcation(@ForAll Interval32Box x,
                                     @ForAll Interval32Box y) {
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 2);
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
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 3);
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
        } else if (y.upperBound() == 0) {
            high = maximum(x.lowerBound() / y.lowerBound(),
                           x.upperBound() / y.lowerBound());
        }
        Assertions.assertEquals(low, z.lowerBound());
        Assertions.assertEquals(high, z.upperBound());
    }

    @Property
    void transferCondReturns2Intervals(@ForAll Interval32Box x,
                                       @ForAll Interval32Box y,
                                       @ForAll @IntRange(min=0, max=5) int t) {
        List<Interval32Box> z = IntervalBoxState.transferCond(x, y, (byte) t);
        Assertions.assertEquals(2, z.size());
    }

    @Property
    void transferConditionEquals(@ForAll Interval32Box x,
                                 @ForAll Interval32Box y) {
        byte position = x.intersectionPosition(y);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 0);
        if (position == 0 || position == 4) {
            for (Interval32Box z : zs) {
                Assertions.assertTrue(z.isBottom());
            }
        } else if (position == 1) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(y.lowerBound(), z.lowerBound());
                Assertions.assertEquals(x.upperBound(), z.upperBound());
            }
        } else if (position == 2) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(y, z);
            }
        } else if (position == 3) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(x.lowerBound(), z.lowerBound());
                Assertions.assertEquals(y.upperBound(), z.upperBound());
            }
        } else if (position == 5) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(x, z);
            }
        }
    }

    @Property
    void transferConditionNotEquals(@ForAll Interval32Box x,
                                    @ForAll Interval32Box y) {
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 1);
        if (x.lowerBound() == x.upperBound() &&
            x.upperBound() == y.lowerBound() &&
            y.lowerBound() == y.upperBound()) {
            for (Interval32Box z : zs) {
                Assertions.assertTrue(z.isBottom());
            }
        } else {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(y, zs.get(1));
        }
    }

    @Property
    void transferConditionLTE(@ForAll Interval32Box x,
                              @ForAll Interval32Box y) {
        byte position = x.intersectionPosition(y);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 2);
        if (position == 0 || position == 1) {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(y, zs.get(1));
        } else if (position == 2) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(y.upperBound(), z.upperBound());
            }
            Assertions.assertEquals(x.lowerBound(), zs.get(0).lowerBound());
            Assertions.assertEquals(y.lowerBound(), zs.get(1).lowerBound());
        } else if (position == 3) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(x.lowerBound(), z.lowerBound());
                Assertions.assertEquals(y.upperBound(), z.upperBound());
            }
        } else if (position == 5) {
            Assertions.assertEquals(x.lowerBound(), zs.get(0).lowerBound());
            Assertions.assertEquals(x.upperBound(), zs.get(0).upperBound());
            Assertions.assertEquals(x.lowerBound(), zs.get(1).lowerBound());
            Assertions.assertEquals(y.upperBound(), zs.get(1).upperBound());
        }
    }

    @Property
    void transferConditionLT(@ForAll Interval32Box x,
                             @ForAll Interval32Box y) {
        byte position = x.intersectionPosition(y);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 3);
        if (position == 0 || position == 1) {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(y, zs.get(1));
        } else if (position == 2) {
            if (x.lowerBound().equals(x.upperBound()) &&
                x.upperBound().equals(y.lowerBound()) &&
                y.lowerBound().equals(y.upperBound())) {
                for (Interval32Box z : zs) {
                    Assertions.assertTrue(z.isBottom());
                }
            } else {
                Assertions.assertEquals(x.lowerBound(), zs.get(0).lowerBound());
                Assertions.assertEquals(y.upperBound() -1, zs.get(0).upperBound());
                if (x.lowerBound().equals(y.lowerBound())) {
                    Assertions.assertEquals(y.lowerBound() + 1, zs.get(1).lowerBound());
                } else {
                    Assertions.assertEquals(y.lowerBound(), zs.get(1).lowerBound());
                }
                Assertions.assertEquals(y.upperBound(), zs.get(1).upperBound());
            }
        } else if (position == 3) {
            Assertions.assertEquals(x.lowerBound(), zs.get(0).lowerBound());
            Assertions.assertEquals(y.upperBound() - 1, zs.get(0).upperBound());
            Assertions.assertEquals(x.lowerBound() + 1, zs.get(1).lowerBound());
            Assertions.assertEquals(y.upperBound(), zs.get(1).upperBound());
        } else if (position == 4) {
            for (Interval32Box z : zs) {
                Assertions.assertTrue(z.isBottom());
            }
        } else if (position == 5) {
            Assertions.assertEquals(x.lowerBound(), zs.get(0).lowerBound());
            Assertions.assertEquals(x.upperBound(), zs.get(0).upperBound());
            Assertions.assertEquals(x.lowerBound() + 1, zs.get(1).lowerBound());
            Assertions.assertEquals(y.upperBound(), zs.get(1).upperBound());
        }
    }

    @Property
    void transferConditionGTE(@ForAll Interval32Box x,
                              @ForAll Interval32Box y) {
        byte position = x.intersectionPosition(y);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 4);
        if (position == 0) {
            for (Interval32Box z : zs) {
                Assertions.assertTrue(z.isBottom());
            }
        } else if (position == 1) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(y.lowerBound(), z.lowerBound());
                Assertions.assertEquals(x.upperBound(), z.upperBound());
            }
        } else if (position == 2) {
            Assertions.assertEquals(y.lowerBound(), zs.get(0).lowerBound());
            Assertions.assertEquals(x.upperBound(), zs.get(0).upperBound());
            Assertions.assertEquals(y, zs.get(1));
        } else if (position == 3 || position == 4) {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(y, zs.get(1));
        } else if (position == 5) {
            for (Interval32Box z : zs) {
                Assertions.assertEquals(x, z);
            }
        }
    }

    @Property
    void transferConditionGT(@ForAll Interval32Box x,
                             @ForAll Interval32Box y) {
        byte position = x.intersectionPosition(y);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 5);
        if (position == 0) {
            for (Interval32Box z : zs) {
                Assertions.assertTrue(z.isBottom());
            }
        } else if (position == 1) {
            Assertions.assertEquals(y.lowerBound() + 1, zs.get(0).lowerBound());
            Assertions.assertEquals(x.upperBound(), zs.get(0).upperBound());
            Assertions.assertEquals(y.lowerBound(), zs.get(1).lowerBound());
            Assertions.assertEquals(x.upperBound() - 1, zs.get(1).upperBound());
        } else if (position == 2) {
            if (x.lowerBound().equals(x.upperBound()) &&
                x.upperBound().equals(y.lowerBound()) &&
                y.lowerBound().equals(x.upperBound())) {
                for (Interval32Box z : zs) {
                    Assertions.assertTrue(z.isBottom());
                }
            } else {
                Assertions.assertEquals(y.lowerBound() + 1, zs.get(0).lowerBound());
                Assertions.assertEquals(x.upperBound(), zs.get(0).upperBound());
                Assertions.assertEquals(y.lowerBound(), zs.get(1).lowerBound());
                Assertions.assertEquals(y.upperBound(), zs.get(1).upperBound());
            }
        } else if (position == 3 || position == 4) {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(y, zs.get(1));
        } else if (position == 5) {
            Assertions.assertEquals(x, zs.get(0));
            Assertions.assertEquals(x.lowerBound() + 1, zs.get(1).lowerBound());
            Assertions.assertEquals(x.upperBound() - 1, zs.get(1).upperBound());
        }
    }

    @Property
    void toSMTFormula(@ForAll Map<Local, Interval32Box> states) {
        Set<Local> locals = states.keySet();
        IntervalBoxState state = new IntervalBoxState(locals, false);
        states.forEach((l, b) -> state.update(l, b));
        List<String> expected = states.entrySet().stream().map(e -> {
                Local l = e.getKey();
                Interval32Box b = e.getValue();
                return String.format("%s->%s", l, b.toSMTFormula(l.toString()));
            }).sorted().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<String> result = Arrays.stream(state.toSMTFormula().split("\n"))
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
