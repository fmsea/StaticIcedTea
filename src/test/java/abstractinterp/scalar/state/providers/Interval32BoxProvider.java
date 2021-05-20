package abstractinterp.scalar.state.providers;


import abstractinterp.scalar.state.Interval32Box;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class Interval32BoxProvider implements ArbitraryProvider {

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Interval32Box.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        Random r = new Random();
        int partition = r.nextInt(max);
        Arbitrary<Integer> lower = Arbitraries.integers().between(min, partition);
        Arbitrary<Integer> upper = Arbitraries.integers().between(partition, max);
        Arbitrary<Interval32Box> randomIntervals = Combinators.combine(lower, upper)
            .as(Interval32Box::new);
        Arbitrary<Interval32Box> singletons =
            Arbitraries.frequencyOf(Tuple.of(1, Arbitraries.just(new Interval32Box(0))),
                                    Tuple.of(1, Arbitraries.just(new Interval32Box(partition))),
                                    Tuple.of(1, lower.flatMap(x -> Arbitraries.just(new Interval32Box(x)))),
                                    Tuple.of(1, upper.flatMap(x -> Arbitraries.just(new Interval32Box(x)))));
        Arbitrary<Interval32Box> rangeOne =
            Arbitraries.frequencyOf(Tuple.of(1, Arbitraries.just(new Interval32Box(-1, 0))),
                                    Tuple.of(1, Arbitraries.just(new Interval32Box(0, 1))));
        return Collections.singleton(Arbitraries.frequencyOf(Tuple.of(5, randomIntervals),
                                                             Tuple.of(1, singletons),
                                                             Tuple.of(1, rangeOne)));
    }
}
